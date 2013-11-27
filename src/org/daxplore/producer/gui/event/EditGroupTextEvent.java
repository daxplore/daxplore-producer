package org.daxplore.producer.gui.event;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;

public class EditGroupTextEvent {
	private MetaGroup metaGroup;
	
	public EditGroupTextEvent(MetaGroup metaGroup) {
		this.metaGroup = metaGroup;
	}
	
	public MetaGroup getMetaGroup() {
		return metaGroup;
	}
}
