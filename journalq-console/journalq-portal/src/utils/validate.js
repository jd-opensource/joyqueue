export function isvalidUsername (str) {
  const validMap = ['admin', 'editor']
  return validMap.indexOf(str.trim()) >= 0
}

/* 合法uri */
export function validateURL (textval) {
  const urlregex = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return urlregex.test(textval)
}

/* 小写字母 */
export function validateLowerCase (str) {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/* 大写字母 */
export function validateUpperCase (str) {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/* 大小写字母 */
export function validatAlphabets (str) {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

export function isNumber (val) {
  var regPos = /^\d+(\.\d+)?$/ // 非负浮点数
  var regNeg = /^(-(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*)))$/ // 负浮点数
  if (regPos.test(val) || regNeg.test(val)) {
    return true
  } else {
    return false
  }
}

/**
 * 判断是否是字母，数字，小数点，下划线
 * @param val
 * @returns {boolean}
 */
export function isKeyString (val) {
  let reg = /^[0-9a-zA-Z\.\_]+$/
  if (reg.test(val)) {
    return true
  } else {
    return false
  }
}

/**
 * 判断是否纯字母
 * @param val
 * @returns {boolean}
 */
export function isKeyStartString (val) {
  let reg = /^[a-zA-Z]+$/
  if (reg.test(val)) {
    return true
  } else {
    return false
  }
}

/**
 * 以英文字母开头，英文字母、阿拉伯数字、下划线和小数点组成,不能超过64个字符
 * @param val
 * @returns {boolean}
 */
export function isCode (value) {
  let valueStart = value.substr(0, 1)
  if (value.length > 64) {
    return false
  } else if (!isKeyStartString(valueStart)) {
    return false
  } else if (!isKeyString(value)) {
    return false
  }
  return true
}
