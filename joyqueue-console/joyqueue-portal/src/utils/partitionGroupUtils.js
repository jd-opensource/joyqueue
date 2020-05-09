export default function mergePartitionGroup (group) {
  let groupStr = ''
  let start = 0
  let end = 0
  if (group.length > 0) {
    start = group[0]
    groupStr = '[' + start
  }
  for (let i = 1; i < group.length; i++) {
    if (group[i] - group[i - 1] === 1) {
      end = group[i]
    } else {
      if (start < end) {
        groupStr += ('-' + end + '],[' + group[i])
      } else {
        groupStr += '],'
        if (i !== group.length - 1) {
          groupStr += ('[' + start)
        }
      }
      start = group[i]
    }
  }
  if (groupStr.endsWith(',[')) {
    groupStr = groupStr.substring(0, groupStr.length - 2)
  } else {
    if (start === end || start > end) {
      groupStr = groupStr + ']'
    } else {
      groupStr = groupStr + '-' + end + ']'
    }
  }
  return groupStr
}
