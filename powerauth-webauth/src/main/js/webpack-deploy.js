/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const fs = require('fs');

let compilationStart = -1;
let compilationEnd = -1;

/**
 * This plug-in handles redeployment of the JavaScript code during development.
 * @constructor empty
 */
function WebpackDeployPlugin() {
}

WebpackDeployPlugin.prototype.apply = function (compiler) {
    compiler.plugin("compilation", compilation => {
        const date = new Date();
        compilationStart = date.getTime();
        console.log("Compilation started.");
    });

    compiler.plugin("done", compilation => {
        const date = new Date();
        compilationEnd = date.getTime();
        console.log("Compilation finished in " + (compilationEnd - compilationStart) + " ms.");
        try {
            const src1 = "src/main/resources/static/built/bundle.js";
            const src2 = "src/main/resources/static/built/bundle.js.map";
            const target1 = "target/classes/static/built/bundle.js";
            const target2 = "target/classes/static/built/bundle.js.map";
            // redeploy only when source and target files exist
            fs.accessSync(src1);
            fs.accessSync(src2);
            fs.accessSync(target1);
            fs.accessSync(target2);
            console.log("Deploying bundle.js and bundle.js.map");
            const rs1 = fs.createReadStream(src1);
            const rs2 = fs.createReadStream(src2);
            const ws1 = fs.createWriteStream(target1);
            const ws2 = fs.createWriteStream(target2);
            rs1.pipe(ws1);
            ws1.on('finish', function () {
                rs2.pipe(ws2);
                ws2.on('finish', function () {
                    console.log('Deployment was successful.');
                });
            });
        } catch (ex) {
            console.log("Error occurred during deployment: "+ex.toString());
        }
    });
};

module.exports = WebpackDeployPlugin;