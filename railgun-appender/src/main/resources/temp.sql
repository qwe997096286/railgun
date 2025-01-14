-- Create table
create table STUDENT.bsh_stu_base_info
(
  stuid      varchar2(11) not null,--学号：'S'+班号（7位数）+学生序号（3位数）(1)
  stuname    varchar2(50) not null,--学生姓名
  sex        char(1) not null,--性别
  age        number(2) not null,--年龄
  classno    varchar2(7) not null,--班号：'C'+年级（4位数）+班级序号（2位数）
  stuaddress varchar2(100) default '地址未录入',--地址 (2)
  grade      char(4) not null,--年级
  enroldate  date,--入学时间
  idnumber   varchar2(18) default '身份证未采集' not null--身份证
)
tablespace USERS --(3)
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table
comment on table STUDENT.bsh_stu_base_info --(4)
  is '学生信息表';
-- Add comments to the columns
comment on column STUDENT.bsh_stu_base_info.stuid -- (5)
  is '学号';
comment on column STUDENT.bsh_stu_base_info.stuname
  is '学生姓名';
comment on column STUDENT.bsh_stu_base_info.sex
  is '学生性别';
comment on column STUDENT.bsh_stu_base_info.age
  is '学生年龄';
comment on column STUDENT.bsh_stu_base_info.classno
  is '学生班级号';
comment on column STUDENT.bsh_stu_base_info.stuaddress
  is '学生住址';
comment on column STUDENT.bsh_stu_base_info.grade
  is '年级';
comment on column STUDENT.bsh_stu_base_info.enroldate
  is '入学时间';
comment on column STUDENT.bsh_stu_base_info.idnumber
  is '身份证号';
