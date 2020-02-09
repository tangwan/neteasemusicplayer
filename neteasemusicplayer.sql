create database neteasemusicplayer;
use neteasemusicplayer;
create table users
(
id varchar(20) primary key,
password varchar (20) not null ,
name varchar (30) default '空',
image_url varchar(100) default 'http://localhost:8080/neteasemusicplayerserver_war_exploded/image/UserDefaultImage.png'
);
drop table users;
update users set image_url='http://localhost:8080/neteasemusicplayerserver_war_exploded/image/UserDefaultImage.png' where id=13025583130;
insert into users(id,password) values('13025583130','10010');
select * from users;
update users set name='lollipop' where id='13025583130';

create table songgroups
(
id int primary key auto_increment,
name varchar (20),
user_id varchar(20) references users(id)
);
create table songs 
(
id int(5) primary key auto_increment,
name varchar (25),
singer varchar(25),
album varchar (25),
total_time varchar(10),
size float,
resource varchar (150),
lyrics varchar (150)
);
insert into songs(name,singer,album,total_time,size,resource) values('可惜没如果','林俊杰','可惜没如果','04:58',52.6,'http://localhost:8080/neteasemusicplayerserver_war_exploded/localSong/林俊杰-可惜没如果.wav');
insert into songs(name,singer,album,total_time,size,resource) values('爱不会绝迹','林俊杰','爱不会绝迹','04:00',42.4,'http://localhost:8080/neteasemusicplayerserver_war_exploded/localSong/林俊杰-爱不会绝迹.wav');
select * from songs;
drop table songs;

create table singers
(
id int primary key auto_increment,
name varchar(25),
image_url varchar(100)
);
drop table singers;
insert into singers(name,image_url) values('Westlife','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/Westlife.png');
insert into singers(name,image_url) values('周杰伦','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/zhoujielun.png');
insert into singers(name,image_url) values('周柏豪','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/zhoubaihao.png');
insert into singers(name,image_url) values('罗志祥','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/luozhixiang.png');
insert into singers(name,image_url) values('许嵩','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/xusong.png');
insert into singers(name,image_url) values('G.E.M.邓紫棋','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/G.E.M.png');
insert into singers(name,image_url) values('陈柏宇','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/chenbaiyu.png');
insert into singers(name,image_url) values('林俊杰','http://localhost:8080/neteasemusicplayerserver_war_exploded/image/singer/linjunjie.png');
select * from singers;