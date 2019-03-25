var path = require('path');
var gulp = require('gulp');
var del = require('del');
var runSequence = require('gulp-sequence');
var htmlreplace = require('gulp-html-replace');


gulp.task('jmq',function (cb) {
    runSequence('cleanjmq', 'movejmq')(function () {
        cb()
        console.log('dataweb编译完毕');
    });
});

/* -------------------------------------
 下面是子任务
 ----------------------------------------*/

gulp.task('cleanjmq', function (cb) {
    var targetPath = path.resolve(__dirname, '../jmq-web/jmq-web-webroot/src/main/webroot');
    console.log('清空 /jmq 目录');
    return  del([targetPath+'/**/*'],{force:true});
});
gulp.task('movejmq',function () {
    console.log('jmq/dist和html到后端目录');
    gulp.src(['./dist/**','!./dist/index.html'])
        .pipe(gulp.dest(path.resolve(__dirname,  '../jmq-web/jmq-web-webroot/src/main/webroot')));
    gulp.src(['./static/**'])
        .pipe(gulp.dest(path.resolve(__dirname,  '../jmq-web/jmq-web-webroot/src/main/webroot/public')));
    gulp.src('./dist/index.html')
        .pipe(htmlreplace({
            'mock': ''
        }))
        .pipe(gulp.dest(path.resolve(__dirname,  '../jmq-web/jmq-web-webroot/src/main/webroot')));
});
