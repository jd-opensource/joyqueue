/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var path = require('path');
var gulp = require('gulp');
var del = require('del');
var runSequence = require('gulp-sequence');
var htmlreplace = require('gulp-html-replace');


gulp.task('journalq',function (cb) {
    runSequence('cleanjournalq', 'movejournalq')(function () {
        cb()
        console.log('dataweb编译完毕');
    });
});

/* -------------------------------------
 下面是子任务
 ----------------------------------------*/

gulp.task('cleanjournalq', function (cb) {
    var targetPath = path.resolve(__dirname, '../journalq-web/journalq-web-webroot/src/main/webroot');
    console.log('清空 /journalq 目录');
    return  del([targetPath+'/**/*'],{force:true});
});
gulp.task('movejournalq',function () {
    console.log('journalq/dist和html到后端目录');
    gulp.src(['./dist/**','!./dist/index.html'])
        .pipe(gulp.dest(path.resolve(__dirname,  '../journalq-web/journalq-web-webroot/src/main/webroot')));
    gulp.src(['./static/**'])
        .pipe(gulp.dest(path.resolve(__dirname,  '../journalq-web/journalq-web-webroot/src/main/webroot/public')));
    gulp.src('./dist/index.html')
        .pipe(htmlreplace({
            'mock': ''
        }))
        .pipe(gulp.dest(path.resolve(__dirname,  '../journalq-web/journalq-web-webroot/src/main/webroot')));
});
