#!/bin/bash 

DATE=`date +%d%b%y`
LOCAL_BACKUP_DIR="backup/"
DB_HOST="127.0.0.1"
DB_NAME="dbname"
DB_USER="dbuser"
DB_PASSWORD="password"
LOG_FILE=backup/backup-$DATE.log
BACKUP_SERVER_ENDPOINT="http://host/api/receiver/folder"

############### Local Backup  ########################
 
mysqldump -h $DB_HOST -u $DB_USER  -p$DB_PASSWORD $DB_NAME | gzip  > $LOCAL_BACKUP_DIR/$DB_NAME-$DATE.sql.gz
 

############### UPLOAD to Backup App Server  ################

curl -F "file=@$LOCAL_BACKUP_DIR/$DB_NAME-$DATE.sql.gz" $BACKUP_SERVER_ENDPOINT 

echo "Database Successfully Uploaded to Ftp Server
      File Name $DB_NAME-$DATE.sql.gz " > $LOG_FILE