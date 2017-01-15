/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.event;

/**
 * A collection of empty events sent over the EventBus.
 */
public class EmptyEvents {
	
	public static class DiscardChangesEvent {}
	
	public static class ExportTextsEvent {}

	public static class ExportUploadEvent {}
	
	public static class HistoryGoBackEvent {}

	public static class ImportSpssEvent {}

	public static class ImportTextsEvent {}
	
	public static class LocaleAddedOrRemovedEvent {}

	public static class QuitProgramEvent {}
	
	public static class RawImportEvent {}

	public static class ReloadTextsEvent {}
	
	public static class RepaintWindowEvent {}

	public static class SaveFileEvent {}
	
}
