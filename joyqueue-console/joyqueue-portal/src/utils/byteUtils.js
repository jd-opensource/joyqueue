export default function bytesToSize (bytes, decimals = 2) {
  if (bytes === 0) return '0 Bytes/s'
  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['Bytes/s', 'KB/s', 'MB/s', 'GB/s', 'TB/s', 'PB/s', 'EB/s', 'ZB/s', 'YB/s']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
}
