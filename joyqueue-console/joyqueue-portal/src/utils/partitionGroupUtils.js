export default function mergePartitionGroup (group) {
  group.sort((v1, v2) => v1 - v2)
  let allList = []
  let tmpList = []
  if (group.length >= 0) {
    tmpList.push(group[0])
  }
  if (group.length === 1) {
    allList.push(tmpList)
  }
  for (let i = 1; i < group.length; i++) {
    if (group[i] - group[i - 1] !== 1) {
      if (tmpList.indexOf(group[i - 1]) === -1) {
        tmpList.push(group[i - 1])
      }
      let list = []
      Object.assign(list, tmpList)
      allList.push(list)
      tmpList = []
      tmpList.push(group[i])
    }
    if (i === group.length - 1) {
      if (tmpList.indexOf(group[i]) === -1) {
        tmpList.push(group[i])
      }
      allList.push(tmpList)
    }
  }
  let groupStr = ''
  for (let i in allList) {
    if (groupStr !== '') {
      groupStr += ','
    }
    if (allList.hasOwnProperty(i)) {
      if (allList[i].length > 1) {
        groupStr += '[' + allList[i][0] + '-' + allList[i][allList[i].length - 1] + ']'
      } else if (allList[i].length === 1) {
        groupStr += '[' + allList[i][0] + ']'
      }
    }
  }
  return groupStr
}
