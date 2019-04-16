#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import os
import sys
import time
import hmac
import urllib
import base64
import hashlib
import requests
import datetime
import traceback
import optparse


class Jsutil():
    def __init__(self, host, access_key, secret_key):
        self.access_key = access_key
        self.secret_key = secret_key
        self.host = host

    def upload(self, local_file, bucket_path):
        ''' upload local file to oss '''
        try:
            # compute md5
            file_hash = hashlib.md5()
            with open(local_file, 'r') as f:
                for line in f.readlines():
                    file_hash.update(line)
            content_md5 = file_hash.hexdigest()
            # GMT DateTime
            GMT_FORMAT = '%a, %d %b %Y %H:%M:%S GMT'
            date = datetime.datetime.utcnow().strftime(GMT_FORMAT)
            # generate signature
            content_type = 'application/octet-stream'
            oss_file_path = os.path.join(bucket_path, os.path.basename(local_file))
            signature = self._generate_signature('PUT', content_md5, content_type, date, oss_file_path)
            url = 'http://%s/%s' % (self.host, oss_file_path)
            headers = {"Content-Type":"application/octet-stream",
                "Content-MD5": content_md5,
                "Connection":"Keep-Alive",
                "Date":date,
                "Authorization": "jingdong %s:%s" % (self.access_key, signature)}
            print headers
            print url
            resp = requests.put(url, headers=headers, data=open(local_file, 'rb'))

            if resp.status_code != 200:
                print 'Upload Failed: %s' % resp.text
        except:
            print 'Upload file error: %s' % traceback.format_exc()

    def download(self, resource_path):
        '''
        下载方式有两种：
        1.浏览器使用外链下载
        2.脚本环境，携带header下载
        此处为第二种下载方式
        '''
        # GMT DateTime
        GMT_FORMAT = '%a, %d %b %Y %H:%M:%S GMT'
        date = datetime.datetime.utcnow().strftime(GMT_FORMAT)
        signature = self._generate_signature('GET', '', '', date, resource_path)
        url = 'http://%s/%s' % (self.host, resource_path)
        headers = {'Date': date, 'Authorization': 'jingdong %s:%s' % (self.access_key, signature)}
        try:
            resp = requests.get(url, headers=headers, stream=True)
            with open(url.split('/')[-1], 'wb') as f:
                for chunk in resp.iter_content(chunk_size=1024):
                    if chunk:
                        f.write(chunk)
        except:
            print 'Download Error: %s' % traceback.format_exc()


    def generate_download_url(self, expire, resource_path):
        '''生成下载外链'''
        expire = int(time.time()) + expire
        signature = urllib.quote(self._generate_signature('GET', '', '', expire, resource_path))
        url = 'http://{host}/{resource}?Expires={Expires}&AccessKey={AccessKey}&Signature={Signature}'.format(
            host = self.host,
            resource = resource_path,
            Expires = expire,
            AccessKey = self.access_key,
            Signature = signature)

        return url

    def _generate_signature(self, http_method, content_md5, content_type, time, resource):
        str_to_sign = '{http_method}\n{content_md5}\n{content_type}\n{time}\n/{resource}'.format(
            http_method = http_method,
            content_md5 = content_md5,
            content_type = content_type,
            time = time,
            resource = resource)
        print str_to_sign
        hmac_sha1 = hmac.new(self.secret_key, str_to_sign, hashlib.sha1).digest()
        signature = base64.b64encode(hmac_sha1)

        return signature

def _main():
    parser = optparse.OptionParser()
    parser.add_option("-f", '--local_file', default = '', help = "Local file, eg:/export/logs/test.txt")
    parser.add_option("-a", '--access_key', help = 'AccessKey of your bucket')
    parser.add_option("-s", '--secret_key', help = 'SecretKey of your bucket')
    parser.add_option('-H', '--host', default = 'storage.jd.local', help = 'Jss host')
    parser.add_option('-r', '--resource', help = 'Resource path: folder path if upload, eg: dist-test/test/, file path if download, eg: dist-test/test/test.txt')

    (options, args) = parser.parse_args()
    if not options.access_key or not options.secret_key or not options.resource:
        parse.error('Incompleted info: access_key, secret_key and resource are necessary.')
    #access key:dyOnweqqzPzU3fbx
    #secret key:mVLGu6a6FVsxFCUEea1l6zoGsgP2okaqIdZ28ju7
    # python ./jsutil --access_key dyOnweqqzPzU3fbx --secret_key mVLGu6a6FVsxFCUEea1l6zoGsgP2okaqIdZ28ju7 --host test.storage.jd.com --localfile /export/docs/ucc.md -r docker
    local_file = options.local_file
    access_key = options.access_key
    secret_key = options.secret_key
    host = options.host
    resource = options.resource

    jss = Jsutil(host, access_key, secret_key)
    # 示例：上传文件
    if options.local_file:
        upload = jss.upload(local_file, resource)
    # 示例：下载文件
    else:
        download = jss.download(resource)
    return 0
    
#  test cases
#  python ./jsutil.py --access_key dyOnweqqzPzU3fbx --secret_key mVLGu6a6FVsxFCUEea1l6zoGsgP2okaqIdZ28ju7 --host test.storage.jd.com  -r docker/ucc.md
#  python ./jsutil.py --access_key dyOnweqqzPzU3fbx --secret_key mVLGu6a6FVsxFCUEea1l6zoGsgP2okaqIdZ28ju7 --host test.storage.jd.com --local_file /export/docs/ucc.md -r docker
if __name__ == "__main__":
    _main()
