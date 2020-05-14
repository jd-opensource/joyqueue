export default function bytesToSize (bytes, decimals = 2, isTotal = false) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  let flowRate = parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
  if (!isTotal) {
    return flowRate + '/s'
  }
  return flowRate
}
