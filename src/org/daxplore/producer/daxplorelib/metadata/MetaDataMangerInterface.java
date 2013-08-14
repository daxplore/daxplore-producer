package org.daxplore.producer.daxplorelib.metadata;

import java.util.List;

public interface MetaDataMangerInterface<T, I> {

	public void init();
	
	public T create(I id);
	
	public T get(I id);
	
	public void remove(I id);
	
	public void saveAll();
	
	public List<T> getAll();
	
}
