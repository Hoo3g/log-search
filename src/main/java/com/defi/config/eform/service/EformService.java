package com.defi.config.eform.service;

import com.defi.config.eform.dto.EformFilter;
import com.defi.config.eform.dto.EformPageResult;
import com.defi.config.eform.entity.Eform;
import org.jdbi.v3.core.Handle;

import java.util.List;

public interface EformService {

    // Basic CRUD operations
    Eform create(Eform eform);

    boolean update(Eform eform);

    boolean delete(String code);

    Eform getByCode(String code);

    boolean existsByCode(String code);

    List<Eform> listAll();

    EformPageResult filter(EformFilter filter);

    // Handle-based methods for transaction support
    Eform create(Handle handle, Eform eform);

    boolean update(Handle handle, Eform eform);

    boolean delete(Handle handle, String code);

    Eform getByCode(Handle handle, String code);

    boolean existsByCode(Handle handle, String code);

    List<Eform> listAll(Handle handle);

    EformPageResult filter(Handle handle, EformFilter filter);
}