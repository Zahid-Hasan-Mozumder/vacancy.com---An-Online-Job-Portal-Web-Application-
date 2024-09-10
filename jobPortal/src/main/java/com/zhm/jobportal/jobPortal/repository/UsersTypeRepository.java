package com.zhm.jobportal.jobPortal.repository;

import com.zhm.jobportal.jobPortal.entity.UsersType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UsersTypeRepository extends JpaRepository<UsersType, Integer> {

}
