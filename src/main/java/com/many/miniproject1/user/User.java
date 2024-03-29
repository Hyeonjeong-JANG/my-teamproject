package com.many.miniproject1.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Table(name = "user_tb")
@Data
@Entity // 테이블 생성하기 위해 필요한 어노테이션
public class User {
    @Id // PK 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment 전략
    private Integer id;
    private String role; // perso=-n, \]

    private String email;
    private String password;
    private String username;
    private String tel;
    private String companyName;
    private String address;
    private String companyNum;
    private String profile;
    private String birth;
    private Timestamp createdAt;
}
