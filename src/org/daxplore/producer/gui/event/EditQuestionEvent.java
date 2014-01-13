/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.event;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;

public class EditQuestionEvent {
	MetaQuestion metaQuestion;

	public EditQuestionEvent(MetaQuestion metaQuestion) {
		this.metaQuestion = metaQuestion;
	}
	
	public MetaQuestion getMetaQuestion() {
		return metaQuestion;
	}
}
