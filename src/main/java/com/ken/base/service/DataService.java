package com.ken.base.service;

import com.ken.base.dao.InnerData;

/**
 * Created by ken on 19/4/16.
 */
public interface DataService {
    public InnerData insert(InnerData in);
    public InnerData update(InnerData in);
    public InnerData delete(InnerData in);
    public InnerData query(InnerData in);
}
