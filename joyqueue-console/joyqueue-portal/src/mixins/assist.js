export function lableObjfromArrStr (arrStr) {
  let arr = typeof(arrStr) === 'string' ? JSON.parse(arrStr) : arrStr;
  let obj = {};
  for(let i in arr){
    for(let k in arr[i]){
      if(arr[i].hasOwnProperty(k)){
        obj[k] = arr[i][k]
      }
    }
  }
  return obj;
}

export function lableStrFromObj(obj){
  let arr = [];
  let temp;
  for(let k in obj){
    temp = {}
    temp[k] = obj[k];
    arr.push(temp)
  }
  return JSON.stringify(arr);
}
