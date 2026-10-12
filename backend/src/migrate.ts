import 'dotenv/config'; import fs from 'node:fs'; import {Pool} from 'pg';
const p=new Pool({connectionString:process.env.DATABASE_URL}); await p.query(fs.readFileSync('schema.sql','utf8')); await p.end();
