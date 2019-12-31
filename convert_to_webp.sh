# bin/bash

export lujing=$PWD
function compressToWeb(){
	file=$1
	fileName=${file%.*}
	echo "${file##*.}"
    if [ ${file##*.} = "png" && ${fileName##*.} != "9" ]
    then
        echo $fileName
    	cwebp -q 90 $2/$1 -o "$2/${fileName}.webp"
    	rm -f $2/$1
    fi
}

function convertToWeb(){
	for entry in $(ls $1)
	do
		if test -d $1/$entry;then
		  echo "convertToWeb $1/$entry"
          convertToWeb $1/$entry
        else
          echo "compressToWeb $entry"
          compressToWeb $entry $1
        fi		
	done
}

convertToWeb $lujing