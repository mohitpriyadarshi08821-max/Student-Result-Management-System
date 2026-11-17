#!/usr/bin/env node
const { spawn } = require('child_process');
const path = require('path');

const jarPath = path.join(__dirname, 'target', 'student-result-management-system-1.0.0.jar');
const javaCmd = process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME, 'bin', 'java') : 'java';

const child = spawn(javaCmd, ['-jar', jarPath, ...process.argv.slice(2)], {
  stdio: 'inherit'
});

child.on('exit', (code) => {
  process.exit(code || 0);
});
