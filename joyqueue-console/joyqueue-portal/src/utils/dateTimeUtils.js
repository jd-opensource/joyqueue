/**
 *data类型数据转字符串
 * @param fmt
 * @returns {*}
 * @constructor
 */
Date.prototype.Format = function (fmt) { // author: meizz
  var o = {
    'M+': this.getMonth() + 1, // 月份
    'd+': this.getDate(), // 日
    'h+': this.getHours(), // 小时
    'm+': this.getMinutes(), // 分
    's+': this.getSeconds(), // 秒
    'q+': Math.floor((this.getMonth() + 3) / 3), // 季度
    'S': this.getMilliseconds() // 毫秒
  }
  if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length))
  for (var k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
    }
  }
  return fmt
}

/**
 * 时间戳转日期
 * @param nS
 * @returns {*}
 */

export function timeStampToDate (nS) {
  var data = new Date()
  data.setTime(nS)
  return data
}

/**
 * 毫秒时间戳转换字符串
 * @param nS
 * type false:ns为空的时候，默认当前时间，true，ns为空的时候，返回“-”
 * isHour false , 展示日期，true，只展示小时
 * @returns {*}
 */
export function timeStampToString (nS, type, isHour) {
  var data = new Date()
  if (nS) {
    data = timeStampToDate(nS)
  } else {
    if (type) {
      return '-'
    }
  }

  var format = 'yyyy-MM-dd hh:mm:ss'
  if (isHour) {
    format = 'hh:mm:ss'
  }
  return data.Format(format)
}

/**
 * 当前时间yyyyMMddhhmmss
 * @returns {*}
 */
export function getNowTime () {
  let data = new Date()
  return data.Format('yyyyMMddhhmmss')
}

export function timeStampToDayString (nS, type) {
  var data = new Date()
  if (nS) {
    data = timeStampToDate(nS)
  } else {
    if (type) {
      return '-'
    }
  }

  var format = 'yyyy-MM-dd'
  return data.Format(format)
}

// 时间格式化
export function format (time, type) {
  var data = new Date()
  data.setTime(time)
  if (!type) {
    type = 'yyyy-MM-dd hh:mm:ss:S'
  }
  return data.Format(type)
}

/**
 * 字符串转时间戳  '2015-03-05 17:59:01.0';
 * @param Str
 * @constructor
 */
export function stringToTimeStamp (str) {
  str = str.replace(/-/g, '/')
  let timestamp = new Date(str).getTime()
  return timestamp
}
