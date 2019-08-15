import Vue from 'vue'
const isServer = Vue.prototype.$isServer
/**
 * 常用方法
 */
const SPECIAL_CHARS_REGEXP = /([:\-_]+(.))/g
const MOZ_HACK_REGEXP = /^moz([A-Z])/

const trim = function (string) {
  return (string || '').replace(/^[\s\uFEFF]+|[\s\uFEFF]+$/g, '')
}

// 判断参数是否是其中之一
export function oneOf (value, validList) {
  for (let i = 0; i < validList.length; i++) {
    if (value === validList[i]) {
      return true
    }
  }
  // console.error('value: ' + value + ', validList: ' + JSON.stringify(validList));
  return false
}

// 判断是否是颜色
export function isColor (value) {
  const nameReg = /^[A-Za-z]+$/
  const colorReg = /^#([a-fA-F0-9]){3}(([a-fA-F0-9]){3})?$/
  const rgbaReg = /^[rR][gG][bB][aA]\(\s*((25[0-5]|2[0-4]\d|1?\d{1,2})\s*,\s*){3}\s*(\.|\d+\.)?\d+\s*\)$/
  const rgbReg = /^[rR][gG][bB]\(\s*((25[0-5]|2[0-4]\d|1?\d{1,2})\s*,\s*){2}(25[0-5]|2[0-4]\d|1?\d{1,2})\s*\)$/
  return nameReg.test(value) || colorReg.test(value) || rgbaReg.test(value) || rgbReg.test(value)
}

/**
 * [camelCaseToHyphen 将驼峰命名转换为连字符]
 * @param  {[string]} str [驼峰命名的字符串]
 * @return {[string]}     [连字符的字符串]
 */
export function camelCaseToHyphen (str) {
  return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase()
}

export function camelCase (name) {
  return name.replace(SPECIAL_CHARS_REGEXP, function (_, separator, letter, offset) {
    return offset ? letter.toUpperCase() : letter
  }).replace(MOZ_HACK_REGEXP, 'Moz$1')
}

// For Modal scrollBar hidden
let cached
export function getScrollBarSize (fresh) {
  if (isServer) return 0
  if (fresh || cached === undefined) {
    const inner = document.createElement('div')
    inner.style.width = '100%'
    inner.style.height = '200px'

    const outer = document.createElement('div')
    const outerStyle = outer.style

    outerStyle.position = 'absolute'
    outerStyle.top = 0
    outerStyle.left = 0
    outerStyle.pointerEvents = 'none'
    outerStyle.visibility = 'hidden'
    outerStyle.width = '200px'
    outerStyle.height = '150px'
    outerStyle.overflow = 'hidden'

    outer.appendChild(inner)

    document.body.appendChild(outer)

    const widthContained = inner.offsetWidth
    outer.style.overflow = 'scroll'
    let widthScroll = inner.offsetWidth

    if (widthContained === widthScroll) {
      widthScroll = outer.clientWidth
    }

    document.body.removeChild(outer)

    cached = widthContained - widthScroll
  }
  return cached
}

export function getStyle (element, styleName) {
  if (!element || !styleName) return null

  styleName = camelCase(styleName)
  if (styleName === 'float') {
    styleName = 'cssFloat'
  }

  try {
    const computed = document.defaultView.getComputedStyle(element, '')
    return element.style[styleName] || computed ? computed[styleName] : null
  } catch (e) {
    return element.style[styleName]
  }
}

export function getType (v) {
  return Object.prototype.toString.call(v)
}

function typeOf (obj) {
  const map = {
    '[object Boolean]': 'boolean',
    '[object Number]': 'number',
    '[object String]': 'string',
    '[object Function]': 'function',
    '[object Array]': 'array',
    '[object Date]': 'date',
    '[object RegExp]': 'regExp',
    '[object Undefined]': 'undefined',
    '[object Null]': 'null',
    '[object Object]': 'object'
  }
  return map[getType(obj)]
}

// 判断是否是对象
export function isObject (v) {
  return typeOf(v) === 'object'
}

// 判断是否是函数
export function isFunction (source) {
  return typeOf(source) === 'function'
}

// 判断是否是数组
export function isArray (source) {
  return typeOf(source) === 'array'
}

export function deepCopy (data) {
  const type = typeOf(data)
  let obj

  if (type === 'array') {
    obj = []
  } else if (type === 'object') {
    obj = {}
  } else {
    return data
  }

  if (type === 'array') {
    for (let i = 0; i < data.length; i++) {
      obj.push(deepCopy(data[i]))
    }
  } else if (type === 'object') {
    for (const i in data) {
      obj[i] = deepCopy(data[i])
    }
  }

  return obj
}

// scrollTop animation
export function scrollTop (el, from = 0, to, duration = 500) {
  if (!window.requestAnimationFrame) {
    window.requestAnimationFrame = (
      window.webkitRequestAnimationFrame ||
      window.mozRequestAnimationFrame ||
      window.msRequestAnimationFrame ||
      function (callback) {
        return window.setTimeout(callback, 1000 / 60)
      }
    )
  }
  const difference = Math.abs(from - to)
  const step = Math.ceil(difference / duration * 50)

  function scroll (start, end, step) {
    if (start === end) return

    let d = (start + step > end) ? end : start + step
    if (start > end) {
      d = (start - step < end) ? end : start - step
    }

    if (el === window) {
      window.scrollTo(d, d)
    } else {
      el.scrollTop = d
    }
    window.requestAnimationFrame(() => scroll(d, end, step))
  }
  scroll(from, to, step)
};

// 判断是否完全相等
export function isEqual (alice, bob) {
  if (alice === bob) return true
  if (alice === null || bob === null || typeOf(alice) !== 'object' || typeOf(bob) !== 'object') {
    return alice === bob
  }

  for (let key in alice) {
    if (!hasOwn(alice, key)) continue
    let aliceValue = alice[key]
    let bobValue = bob[key]
    let aliceType = typeOf(aliceValue)

    if (typeof bobValue === 'undefined') {
      return false
    } else if (aliceType === 'object') {
      if (!isEqual(aliceValue, bobValue)) return false
    } else if (aliceValue !== bobValue) {
      return false
    }
  }
  for (let _key in bob) {
    if (!hasOwn(bob, _key)) continue
    if (typeof alice[_key] === 'undefined') return false
  }

  return true
};

/* istanbul ignore next */
export const on = (function () {
  if (!isServer && document.addEventListener) {
    return function (element, event, handler) {
      if (element && event && handler) {
        element.addEventListener(event, handler, false)
      }
    }
  } else {
    return function (element, event, handler) {
      if (element && event && handler) {
        element.attachEvent('on' + event, handler)
      }
    }
  }
})()

/* istanbul ignore next */
export const off = (function () {
  if (!isServer && document.removeEventListener) {
    return function (element, event, handler) {
      if (element && event) {
        element.removeEventListener(event, handler, false)
      }
    }
  } else {
    return function (element, event, handler) {
      if (element && event) {
        element.detachEvent('on' + event, handler)
      }
    }
  }
})()

/* istanbul ignore next */
export const once = function (el, event, fn) {
  let listener = function () {
    if (fn) {
      fn.apply(this, arguments)
    }
    off(el, event, listener)
  }
  on(el, event, listener)
}

export function hasClass (el, cls) {
  if (!el || !cls) return false
  if (cls.indexOf(' ') !== -1) throw new Error('className should not contain space.')
  if (el.classList) {
    return el.classList.contains(cls)
  }
  return (` ${el.className} `).indexOf(` ${cls} `) > -1
}

export function addClass (el, cls) {
  if (!el) return

  const classes = (cls || '').split(' ')
  let curClass = el.className

  for (let i = 0, j = classes.length; i < j; i++) {
    const clsName = classes[i]
    if (!clsName) continue

    if (el.classList) {
      el.classList.add(clsName)
    } else if (!hasClass(el, clsName)) {
      curClass += ` ${clsName}`
    }
  }
  if (!el.classList) {
    el.className = curClass
  }
}

export function removeClass (el, cls) {
  if (!el || !cls) return
  const classes = cls.split(' ')
  let curClass = ` ${el.className} `

  for (let i = 0, j = classes.length; i < j; i++) {
    const clsName = classes[i]
    if (!clsName) continue

    if (el.classList) {
      el.classList.remove(clsName)
    } else if (hasClass(el, clsName)) {
      curClass = curClass.replace(` ${clsName} `, ' ')
    }
  }
  if (!el.classList) {
    el.className = trim(curClass)
  }
}

export function findComponentUpward (context, componentName, componentNames) {
  if (typeof componentName === 'string') {
    componentNames = [componentName]
  } else {
    componentNames = componentName
  }

  let parent = context.$parent
  let name = parent.$options.name
  while (parent && (!name || componentNames.indexOf(name) < 0)) {
    parent = parent.$parent
    if (parent) name = parent.$options.name
  }

  return parent
}

export function findComponentsUpward (context, componentName, components = []) {
  let parent = context.$parent
  let name = parent.$options.name

  while (parent && name) {
    if (componentName === name) {
      components.push(parent)
    }

    parent = parent.$parent
    if (parent) {
      name = parent.$options.name
    }
  }

  return components
}

export function findComponentDownward (context, componentName) {
  const childrens = context.$children
  let children

  if (childrens.length) {
    childrens.forEach(child => {
      if (child.$options.name === componentName) {
        children = child
      }
    })

    for (let i = 0, len = childrens.length; i < len; i++) {
      const child = childrens[i]
      const name = child.$options.name

      if (name === componentName) {
        children = child
        break
      } else {
        children = findComponentDownward(child, componentName)
        if (children) break
      }
    }
  }

  return children
}

export function findComponentsDownward (context, componentName, components = []) {
  const childrens = context.$children

  if (childrens.length) {
    childrens.forEach(child => {
      const subChildren = child.$children
      const name = child.$options.name

      if (name === componentName) {
        components.push(child)
      }
      if (subChildren.length) {
        const findChildren = findComponentsDownward(child, componentName, components)
        if (findChildren) {
          components.concat(findChildren)
        }
      }
    })
  }

  return components
}

// Find brothers components
export function findBrothersComponents (context, componentName, exceptMe = true) {
  let res = context.$parent.$children.filter(item => {
    return item.$options.name === componentName
  })
  let index = res.findIndex(item => item._uid === context._uid)
  if (exceptMe) res.splice(index, 1)
  return res
}

export function noop () {}

export function hasOwn (obj, key) {
  return hasOwnProperty.call(obj, key)
};

export function isDef (val) {
  return val !== undefined && val !== null
}

export const generateId = function () {
  return Math.floor(Math.random() * 10000)
}

function extend (to, _from) {
  for (let key in _from) {
    to[key] = _from[key]
  }
  return to
};

export function toObject (arr) {
  let res = {}
  for (let i = 0; i < arr.length; i++) {
    if (arr[i]) {
      extend(res, arr[i])
    }
  }
  return res
};

export function toBoolean (val) {
  if (val === '') return val
  return val === 'true' || val === '1'
}

export function getPropByPath (obj, path, strict) {
  let tempObj = obj
  path = path.replace(/\[(\w+)\]/g, '.$1')
  path = path.replace(/^\./, '')

  let keyArr = path.split('.')
  let i = 0
  for (let len = keyArr.length; i < len - 1; ++i) {
    if (!tempObj && !strict) break
    let key = keyArr[i]
    if (key in tempObj) {
      tempObj = tempObj[key]
    } else {
      if (strict) {
        throw new Error('please transfer a valid prop path to form item!')
      }
      break
    }
  }
  return {
    o: tempObj,
    k: keyArr[i],
    v: tempObj ? tempObj[keyArr[i]] : null
  }
};

export function merge (target) {
  for (let i = 1, j = arguments.length; i < j; i++) {
    let source = arguments[i] || {}
    for (let prop in source) {
      if (source.hasOwnProperty(prop)) {
        let value = source[prop]
        if (value !== undefined) {
          target[prop] = value
        }
      }
    }
  }

  return target
};

/**
 * Deep merge two objects.
 * @param target
 * @param ...sources
 */
export function mergeDeep (target, ...sources) {
  if (!sources.length) return target
  const source = sources.shift()

  if (isObject(target) && isObject(source)) {
    for (const key in source) {
      if (isObject(source[key])) {
        if (!target[key]) Object.assign(target, { [key]: {} })
        mergeDeep(target[key], source[key])
      } else {
        Object.assign(target, { [key]: source[key] })
      }
    }
  }

  return mergeDeep(target, ...sources)
};

let scrollBarWidth

export function getScrollBarWidth () {
  if (isServer) return 0
  if (scrollBarWidth !== undefined) return scrollBarWidth

  const outer = document.createElement('div')
  outer.className = 'el-scrollbar__wrap'
  outer.style.visibility = 'hidden'
  outer.style.width = '100px'
  outer.style.position = 'absolute'
  outer.style.top = '-9999px'
  document.body.appendChild(outer)

  const widthNoScroll = outer.offsetWidth
  outer.style.overflow = 'scroll'

  const inner = document.createElement('div')
  inner.style.width = '100%'
  outer.appendChild(inner)

  const widthWithScroll = inner.offsetWidth
  outer.parentNode.removeChild(outer)
  scrollBarWidth = widthNoScroll - widthWithScroll

  return scrollBarWidth
};

export const escapeRegexpString = (value = '') => String(value).replace(/[|\\{}()[\]^$+*?.]/g, '\\$&')

// TODO: use native Array.find, Array.findIndex when IE support is dropped
export const arrayFindIndex = function (arr, pred) {
  for (let i = 0; i !== arr.length; ++i) {
    if (pred(arr[i])) {
      return i
    }
  }
  return -1
}

export const arrayFind = function (arr, pred) {
  const idx = arrayFindIndex(arr, pred)
  return idx !== -1 ? arr[idx] : undefined
}

// coerce truthy value to array
export const coerceTruthyValueToArray = function (val) {
  if (Array.isArray(val)) {
    return val
  } else if (val) {
    return [val]
  } else {
    return []
  }
}

export function isVNode (node) {
  return node !== null && typeof node === 'object' && hasOwn(node, 'componentOptions')
};

export function getFirstComponentChild (children) {
  return children && children.filter(c => c && c.tag)[0]
};

export function debounce (fn, delay) {
  let timer = null
  return function () {
    let self = this
    let args = arguments
    clearTimeout(timer)
    timer = setTimeout(function () {
      fn.apply(self, args)
    }, delay)
  }
};

export const getStackMap = (stack) => {
  const stackMap = {}
  Object.keys(stack).forEach(item => {
    stack[item].forEach(name => {
      stackMap[name] = item
    })
  })
  return stackMap
}

export function get (target, path, defaultValue) {
  if (!path) return target
  var pathArr = path.split('.')
  var targetTemp = target
  pathArr.some(function (item, index) {
    if (targetTemp[item] === undefined) {
      targetTemp = defaultValue
      return true
    } else {
      targetTemp = targetTemp[item]
    }
  })
  return targetTemp
};

export function set (target, path, value) {
  if (!path) return
  let targetTemp = target
  let pathArr = path.split('.')
  pathArr.forEach(function (item, index) {
    if (index === pathArr.length - 1) {
      targetTemp[item] = value
    } else {
      if (!targetTemp[item]) targetTemp[item] = {}
      targetTemp = targetTemp[item]
    }
  })
};

export function setArrayValue (arr, index, value) {
  if (arr[index] !== undefined) {
    arr[index].push(value)
  } else {
    arr[index] = [value]
  }
};
