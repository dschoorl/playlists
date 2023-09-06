-- example setup for the database prior to running the liquibase tool for the first time

create database if not exists playlists;

create user if not exists 'liquibase'@'%' identified by 'liquibase';
grant all privileges on playlists.* to 'liquibase'@'%';

create user if not exists 'pl_user'@'%' identified by 'pl_user';
grant delete, execute, insert, select, update on playlists.* to 'pl_user'@'%';

flush privileges;
