<?php
declare(strict_types=1);

// Load .env once. Existing process environment wins; duplicate .env keys are rejected.
$envFile = dirname(__DIR__).'/.env';
if (is_file($envFile)) {
    foreach (file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) as $line) {
        $line = trim($line);
        if ($line === '' || str_starts_with($line, '#')) continue;
        if (!preg_match('/^([A-Za-z_][A-Za-z0-9_]*)=(.*)$/', $line, $m)) continue;
        $key = $m[1];
        if (array_key_exists($key, $_ENV)) continue;
        $_ENV[$key] = trim($m[2], " \t\"'");
    }
}
function env(string $key, ?string $default = null): string {
    $value = $_ENV[$key] ?? getenv($key);
    return ($value === false || $value === null) ? ($default ?? '') : trim((string)$value);
}
function requestId(): string {
    $value = $_SERVER['HTTP_X_REQUEST_ID'] ?? '';
    return preg_match('/^[A-Za-z0-9._:-]{1,100}$/', $value) ? $value : bin2hex(random_bytes(16));
}
$GLOBALS['request_id'] = requestId();
function jsonOut(mixed $data, int $status = 200): never {
    http_response_code($status); header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['data' => $data], JSON_UNESCAPED_SLASHES); exit;
}
function fail(int $status, string $code, string $message, mixed $details = null): never {
    http_response_code($status); header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => ['code'=>$code, 'message'=>$message, 'details'=>$details, 'request_id'=>$GLOBALS['request_id']]], JSON_UNESCAPED_SLASHES); exit;
}
function config(): void {
    foreach (['DATABASE_URL','DB_USER','DB_PASSWORD','JWT_SECRET','GOOGLE_CLIENT_ID'] as $key) if (env($key) === '') fail(500,'CONFIG_ERROR',"Missing required configuration: $key");
    if (!str_starts_with(strtolower(env('DATABASE_URL')), 'mysql:')) fail(500,'CONFIG_ERROR','DATABASE_URL must use PDO MySQL');
    if (strlen(env('JWT_SECRET')) < 32 || in_array(env('JWT_SECRET'), ['CHANGE_ME','CHANGE_ME_LONG_RANDOM'], true)) fail(500,'CONFIG_ERROR','JWT_SECRET is not configured');
}
function db(): PDO { static $pdo; if ($pdo instanceof PDO) return $pdo; config(); try { $pdo = new PDO(env('DATABASE_URL'), env('DB_USER'), env('DB_PASSWORD'), [PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE=>PDO::FETCH_ASSOC, PDO::ATTR_EMULATE_PREPARES=>false]); $pdo->exec("SET time_zone='+00:00'"); return $pdo; } catch (Throwable) { fail(503,'DATABASE_UNAVAILABLE','Database unavailable'); } }
set_exception_handler(fn(Throwable $e) => fail(500,'INTERNAL_ERROR','Unexpected server error'));
function uuid(): string { $x=random_bytes(16); $x[6]=chr((ord($x[6])&15)|64); $x[8]=chr((ord($x[8])&63)|128); return vsprintf('%s%s-%s-%s-%s-%s%s%s',str_split(bin2hex($x),4)); }
function b64(string $x): string { return rtrim(strtr(base64_encode($x),'+/','-_'),'='); }
function ub(string $x): string { $x=strtr($x,'-_','+/'); return base64_decode($x.str_repeat('=',(4-strlen($x)%4)%4),true) ?: ''; }
function jwt(string $sub, int $ttl=900, string $type='access'): string { config(); $h=b64(json_encode(['alg'=>'HS256','typ'=>'JWT'])); $p=b64(json_encode(['sub'=>$sub,'iat'=>time(),'exp'=>time()+$ttl,'typ'=>$type])); return "$h.$p.".b64(hash_hmac('sha256',"$h.$p",env('JWT_SECRET'),true)); }
function verifyJwt(string $token, string $type): array { config(); $a=explode('.',$token); if(count($a)!==3 || strlen($a[0])>1000 || strlen($a[1])>5000 || strlen($a[2])!==43) fail(401,'UNAUTHORIZED','Invalid session'); $h=json_decode(ub($a[0]),true); $p=json_decode(ub($a[1]),true); $sig=b64(hash_hmac('sha256',"$a[0].$a[1]",env('JWT_SECRET'),true)); if(!is_array($h)||($h['alg']??'')!=='HS256'||($h['typ']??'')!=='JWT'||!is_array($p)||($p['typ']??'')!==$type||!is_string($p['sub']??null)||!preg_match('/^[0-9a-f-]{36}$/i',$p['sub'])||!is_int($p['iat']??null)||$p['iat']>time()+60||!is_int($p['exp']??null)||$p['exp']<=time()||$p['exp']>$p['iat']+31536061||!hash_equals($sig,$a[2])) fail(401,'UNAUTHORIZED','Invalid or expired session'); return $p; }
function user(): string { if(!preg_match('/^Bearer\s+([^\s]+)$/i',$_SERVER['HTTP_AUTHORIZATION']??'',$m)) fail(401,'UNAUTHORIZED','Authentication required'); return verifyJwt($m[1],'access')['sub']; }
function id(string $x): string { if(!preg_match('/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i',$x)) fail(400,'VALIDATION_ERROR','Invalid id'); return strtolower($x); }
function body(): array { $x=json_decode(file_get_contents('php://input') ?: '',true); return is_array($x) ? $x : fail(400,'VALIDATION_ERROR','JSON body required'); }
function required(array $b, array $fields): void { foreach($fields as $f) if(!array_key_exists($f,$b)||$b[$f]===''||$b[$f]===null) fail(400,'VALIDATION_ERROR',"$f required",[['field'=>$f,'reason'=>'required']]); }
function dateValue(mixed $v,string $field,bool $datetime=false): string { $pattern=$datetime?'/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/':'/^\d{4}-\d{2}-\d{2}$/'; if(!is_string($v)||!preg_match($pattern,$v)||strtotime($v)===false) fail(400,'VALIDATION_ERROR',"$field has invalid format"); return $v; }
function google(string $token): array { if($token==='') fail(400,'VALIDATION_ERROR','id_token required'); config(); $ctx=stream_context_create(['http'=>['timeout'=>5,'ignore_errors'=>true]]); $r=@file_get_contents('https://oauth2.googleapis.com/tokeninfo?id_token='.rawurlencode($token),false,$ctx); $p=json_decode($r?:'',true); if(!is_array($p)||($p['aud']??'')!==env('GOOGLE_CLIENT_ID')||empty($p['sub'])||empty($p['email'])||($p['email_verified']??'true')==='false') fail(401,'INVALID_GOOGLE_TOKEN','Google token is invalid'); return $p; }
function mediaConfig(): array { return ['root'=>rtrim(env('MEDIA_STORAGE_ROOT',dirname(__DIR__).'/storage/media'),'/'), 'base'=>rtrim(env('MEDIA_PUBLIC_BASE_URL','https://kostku.miraya.my.id/media'),'/'), 'max_bytes'=>(int)env('MEDIA_MAX_BYTES','524288'), 'max_side'=>(int)env('MEDIA_MAX_SIDE','1280')]; }
function strictRoomPricing(): bool { return env('STRICT_ROOM_PRICING','1')==='1'; }
// Utilitas opsional (nomor token listrik / nomor PDAM) dipakai lokasi maupun kamar.
// Disimpan sebagai string agar nol di depan aman; string kosong → NULL (artinya "Tidak tersedia").
function utilityValue($v): ?string { if($v===null) return null; $s=trim((string)$v); return $s===''?null:$s; }
function utilitiesFromBody(array $b, array $current=[]): array { return ['token_listrik'=>array_key_exists('token_listrik',$b)?utilityValue($b['token_listrik']):utilityValue($current['token_listrik']??null),'pdam_number'=>array_key_exists('pdam_number',$b)?utilityValue($b['pdam_number']):utilityValue($current['pdam_number']??null)]; }
function mediaEntity(string $type,string $id,string $uid): array { $allowed=['tenant','room','payment','expense','property']; if(!in_array($type,$allowed,true)) fail(400,'VALIDATION_ERROR','Jenis media tidak didukung'); $table=match($type){'tenant'=>'tenants','room'=>'rooms','payment'=>'payments','expense'=>'expenses','property'=>'properties'}; $row=own($table,$id,$uid); return [$table,$row]; }
function mediaUpload(string $type,string $id,string $uid): array { [$table,$row]=mediaEntity($type,$id,$uid); $c=mediaConfig(); if(empty($_FILES['file'])||$_FILES['file']['error']!==UPLOAD_ERR_OK) fail(400,'VALIDATION_ERROR','File foto wajib diisi.'); $f=$_FILES['file']; if((int)$f['size']>$c['max_bytes']) fail(413,'FILE_TOO_LARGE','Ukuran foto maksimal 500 KB.'); $info=@getimagesize($f['tmp_name']); $mime=is_array($info)?(string)($info['mime']??''):''; if(!$info||!in_array($mime,['image/jpeg','image/png','image/webp'],true)) fail(400,'UNSUPPORTED_MEDIA','Format foto harus JPG, PNG, atau WebP.'); if($info[0]>4096||$info[1]>4096) fail(400,'VALIDATION_ERROR','Dimensi foto terlalu besar.'); $category=match($type){'tenant'=>'penghuni','room'=>'kamar','payment'=>'pembayaran','expense'=>'pengeluaran','property'=>'lokasi'}; $relativeDir=$uid.'/'.$category.'/'.date('Y/m'); $dir=$c['root'].'/'.$relativeDir; if(!is_dir($dir)&&!mkdir($dir,0750,true)) fail(500,'STORAGE_ERROR','Penyimpanan foto tidak tersedia.'); $name=bin2hex(random_bytes(24)).'.'.($mime==='image/png'?'png':($mime==='image/webp'?'webp':'jpg')); $relative=$relativeDir.'/'.$name; $path=$c['root'].'/'.$relative; if(!move_uploaded_file($f['tmp_name'],$path)) fail(500,'STORAGE_ERROR','Foto gagal disimpan.'); $checksum=hash_file('sha256',$path); $db=db(); $db->beginTransaction(); try { $old=$db->prepare('SELECT relative_path FROM media WHERE owner_id=? AND entity_type=? AND entity_id=? AND deleted_at IS NULL FOR UPDATE'); $old->execute([$uid,$type,$id]); $oldPath=$old->fetchColumn() ?: null; $db->prepare('UPDATE media SET deleted_at=UTC_TIMESTAMP() WHERE owner_id=? AND entity_type=? AND entity_id=? AND deleted_at IS NULL')->execute([$uid,$type,$id]); $mid=uuid(); $db->prepare('INSERT INTO media(id,owner_id,entity_type,entity_id,relative_path,mime_type,bytes,width,height,checksum) VALUES(?,?,?,?,?,?,?,?,?,?)')->execute([$mid,$uid,$type,$id,$relative,$mime,(int)filesize($path),$info[0],$info[1],$checksum]); $db->commit(); if($oldPath) @unlink($c['root'].'/'.ltrim($oldPath,'/')); return ['id'=>$mid,'url'=>$c['base'].'/'.$relative,'mime_type'=>$mime,'bytes'=>(int)filesize($path),'width'=>$info[0],'height'=>$info[1]]; } catch(Throwable $e) { if($db->inTransaction())$db->rollBack(); @unlink($path); throw $e; } }
function mediaDelete(string $type,string $id,string $uid): array { mediaEntity($type,$id,$uid); $c=mediaConfig(); $s=db()->prepare('SELECT relative_path FROM media WHERE owner_id=? AND entity_type=? AND entity_id=? AND deleted_at IS NULL');$s->execute([$uid,$type,$id]);$path=$s->fetchColumn();db()->prepare('UPDATE media SET deleted_at=UTC_TIMESTAMP() WHERE owner_id=? AND entity_type=? AND entity_id=? AND deleted_at IS NULL')->execute([$uid,$type,$id]);if($path){$p=$c['root'].'/'.ltrim($path,'/');if(is_file($p))@unlink($p);}return ['deleted'=>true]; }
function mediaPurge(string $type,string $id,string $uid): void { $c=mediaConfig(); $s=db()->prepare('SELECT relative_path FROM media WHERE owner_id=? AND entity_type=? AND entity_id=?');$s->execute([$uid,$type,$id]);foreach($s->fetchAll() as $r){$p=$c['root'].'/'.ltrim($r['relative_path'],'/');if(is_file($p))@unlink($p);}db()->prepare('DELETE FROM media WHERE owner_id=? AND entity_type=? AND entity_id=?')->execute([$uid,$type,$id]); }
function own(string $table,string $rid,string $uid): array { $allowed=['properties','rooms','tenants','payments','expenses','categories']; if(!in_array($table,$allowed,true)) fail(500,'CONFIG_ERROR','Invalid resource'); $sql=$table==='rooms'?'SELECT r.* FROM rooms r JOIN properties p ON p.id=r.property_id WHERE r.id=? AND p.owner_id=?':($table==='payments'?'SELECT p.* FROM payments p JOIN tenants t ON t.id=p.tenant_id WHERE p.id=? AND p.owner_id=? AND t.owner_id=?':"SELECT * FROM $table WHERE id=? AND owner_id=?"); $s=db()->prepare($sql); $args=$table==='payments'?[$rid,$uid,$uid]:[$rid,$uid]; $s->execute($args); return $s->fetch() ?: fail(404,'NOT_FOUND','Not found'); }
// Menulis satu entri riwayat perubahan data penghuni. Tidak pernah memecah alur tenant:
// bila tabel audit belum dimigrasi, kegagalan diabaikan agar operasi utama tetap sukses.
function audit(string $tenantId,string $uid,string $action,?array $details=null): void { try { db()->prepare('INSERT INTO tenant_audit_log(id,owner_id,tenant_id,action,details) VALUES(?,?,?,?,?)')->execute([uuid(),$uid,$tenantId,$action,$details===null?null:json_encode($details,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES)]); } catch(Throwable) { } }
// Isi default kategori (pemasukan & pengeluaran) bila owner belum punya kategori sama sekali,
// supaya pemilih kategori selalu punya isi sampai ada minimal satu kategori.
function ensureCategories(string $uid): void { $s=db()->prepare('SELECT COUNT(*) FROM categories WHERE owner_id=?'); $s->execute([$uid]); if((int)$s->fetchColumn()>0) return; $defaults=['income'=>['Sewa','Deposit','Denda','Lainnya'],'expense'=>['Listrik/Air','Perbaikan','Kebersihan','Gaji','Internet','Perabot','Lainnya']]; $ins=db()->prepare('INSERT INTO categories(id,owner_id,kind,name,sort_order) VALUES(?,?,?,?,?)'); foreach($defaults as $kind=>$names){ $i=0; foreach($names as $name){ $ins->execute([uuid(),$uid,$kind,$name,$i++]); } } }
// Bangun seluruh data aplikasi dalam satu tempat untuk endpoint GET /snapshot:
// properti → tipe kamar → harga → kamar, lalu penghuni, pembayaran, pengeluaran, kategori.
// Semua baris di-scope ke owner. `$propertyId` opsional untuk membatasi ke satu properti.
function insertSnapshot(string $uid, ?string $propertyId=null): array {
    $base=rtrim(env('MEDIA_PUBLIC_BASE_URL','https://kostku.miraya.my.id/media'),'/');
    $purl="CASE WHEN m.relative_path IS NULL THEN NULL ELSE CONCAT('".$base."/',m.relative_path) END AS photo_url";
    $scope=$propertyId!==null?' AND id=?':'';
    $s=db()->prepare("SELECT p.*,CASE WHEN m.relative_path IS NULL THEN NULL ELSE CONCAT('".$base."/',m.relative_path) END AS logo_url FROM properties p LEFT JOIN media m ON m.entity_id=p.id AND m.entity_type='property' AND m.deleted_at IS NULL WHERE p.owner_id=?".($propertyId!==null?' AND p.id=?':'')." ORDER BY p.created_at DESC");
    $s->execute($propertyId!==null?[$uid,$propertyId]:[$uid]); $properties=$s->fetchAll();
    $s=db()->prepare('SELECT rt.* FROM room_types rt JOIN properties p ON p.id=rt.property_id WHERE p.owner_id=?'.($propertyId!==null?' AND rt.property_id=?':'').' ORDER BY rt.created_at DESC');
    $s->execute($propertyId!==null?[$uid,$propertyId]:[$uid]); $roomTypes=$s->fetchAll();
    $s=db()->prepare('SELECT pr.* FROM room_type_prices pr JOIN room_types rt ON rt.id=pr.room_type_id JOIN properties p ON p.id=rt.property_id WHERE p.owner_id=?'.($propertyId!==null?' AND rt.property_id=?':'').' ORDER BY pr.occupancy,pr.duration_value,pr.duration_unit');
    $s->execute($propertyId!==null?[$uid,$propertyId]:[$uid]); $prices=$s->fetchAll();
    $s=db()->prepare("SELECT r.*,rt.name AS room_type_name,$purl FROM rooms r LEFT JOIN room_types rt ON rt.id=r.room_type_id LEFT JOIN media m ON m.entity_id=r.id AND m.entity_type='room' AND m.deleted_at IS NULL WHERE r.property_id IN (SELECT id FROM properties WHERE owner_id=?".($propertyId!==null?' AND id=?':'').") ORDER BY r.created_at DESC");
    $s->execute($propertyId!==null?[$uid,$propertyId]:[$uid]); $rooms=$s->fetchAll();
    $s=db()->prepare("SELECT t.*,$purl FROM tenants t LEFT JOIN media m ON m.entity_id=t.id AND m.entity_type='tenant' AND m.deleted_at IS NULL WHERE t.owner_id=? AND t.status<>'archived' ORDER BY t.created_at DESC");
    $s->execute([$uid]); $tenants=$s->fetchAll();
    $s=db()->prepare("SELECT p.*,$purl FROM payments p LEFT JOIN media m ON m.entity_id=p.id AND m.entity_type='payment' AND m.deleted_at IS NULL WHERE p.owner_id=? ORDER BY p.paid_at DESC");
    $s->execute([$uid]); $payments=$s->fetchAll();
    $s=db()->prepare("SELECT e.*,$purl FROM expenses e LEFT JOIN media m ON m.entity_id=e.id AND m.entity_type='expense' AND m.deleted_at IS NULL WHERE e.owner_id=? ORDER BY e.spent_at DESC");
    $s->execute([$uid]); $expenses=$s->fetchAll();
    ensureCategories($uid);
    $s=db()->prepare('SELECT * FROM categories WHERE owner_id=? ORDER BY kind,sort_order,name');
    $s->execute([$uid]); $categories=$s->fetchAll();
    return ['properties'=>$properties,'room_types'=>$roomTypes,'room_type_prices'=>$prices,'rooms'=>$rooms,'tenants'=>$tenants,'payments'=>$payments,'expenses'=>$expenses,'categories'=>$categories];
}
