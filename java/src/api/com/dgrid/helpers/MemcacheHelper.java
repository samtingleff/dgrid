package com.dgrid.helpers;

import java.util.Date;
import java.util.Map;

import com.danga.MemCached.MemCachedClient;

public interface MemcacheHelper {

	public boolean add(String key, Object value);

	public boolean add(java.lang.String key, java.lang.Object value, Date expiry);

	public boolean add(java.lang.String key, java.lang.Object value,
			Date expiry, int hashCode);

	public boolean add(java.lang.String key, java.lang.Object value,
			int hashCode);

	public long addOrDecr(java.lang.String key);

	public long addOrDecr(java.lang.String key, long inc);

	public long addOrDecr(java.lang.String key, long inc, int hashCode);

	public long addOrIncr(java.lang.String key);

	public long addOrIncr(java.lang.String key, long inc);

	public long addOrIncr(java.lang.String key, long inc, int hashCode);

	public boolean delete(java.lang.String key);

	public boolean delete(java.lang.String key, Date expiry);

	public boolean delete(java.lang.String key, int hashCode, Date expiry);

	public boolean flushAll();

	public Object get(java.lang.String key);

	public Object get(java.lang.String key, int hashCode);

	public long getCounter(java.lang.String key);

	public long getCounter(java.lang.String key, int hashCode);

	public boolean keyExists(java.lang.String key);

	public boolean replace(java.lang.String key, java.lang.Object value);

	public boolean replace(java.lang.String key, java.lang.Object value,
			Date expiry);

	public boolean replace(java.lang.String key, java.lang.Object value,
			Date expiry, int hashCode);

	public boolean set(java.lang.String key, java.lang.Object value);

	public boolean set(java.lang.String key, java.lang.Object value, Date expiry);

	public boolean set(java.lang.String key, java.lang.Object value,
			Date expiry, int hashCode);

	public boolean set(java.lang.String key, java.lang.Object value,
			int hashCode);

	public Map stats();

	public boolean storeCounter(java.lang.String key, long counter);

	public boolean storeCounter(java.lang.String key, long counter, int hashCode);

	public MemCachedClient getMemCachedClient();
}
