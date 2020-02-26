#!/bin/sh


ability=$1
echo "ability:"
echo ${ability}
python fea_ability.py ${ability}

#检查新生成文件是否存在
if [ -f "./app/build_new.gradle" ];then
rm -rf ./app/build.gradle
mv ./app/build_new.gradle ./app/build.gradle
fi