package com.dgrid.util.io;

import java.util.*;
import java.io.File;
import java.lang.ref.WeakReference;

public class FileMonitor {
	private Timer timer_;

	private HashMap<File, Long> files_;

	private Collection<WeakReference<FileListener>> listeners_;

	public FileMonitor(long pollingInterval) {
		files_ = new HashMap<File, Long>();
		listeners_ = new ArrayList<WeakReference<FileListener>>();

		timer_ = new Timer(true);
		timer_.schedule(new FileMonitorNotifier(), 0, pollingInterval);
	}

	public void stop() {
		timer_.cancel();
	}

	public void addFile(File file) {
		if (!files_.containsKey(file)) {
			long modifiedTime = file.exists() ? file.lastModified() : -1;
			files_.put(file, new Long(modifiedTime));
		}
	}

	public void removeFile(File file) {
		files_.remove(file);
	}

	public void addListener(FileListener fileListener) {
		// Don't add if its already there
		for (WeakReference<FileListener> reference : listeners_) {
			FileListener listener = reference.get();
			if (listener == fileListener)
				return;
		}

		// Use WeakReference to avoid memory leak if this becomes the
		// sole reference to the object.
		listeners_.add(new WeakReference<FileListener>(fileListener));
	}

	public void removeListener(FileListener fileListener) {
		for (Iterator<WeakReference<FileListener>> i = listeners_.iterator(); i
				.hasNext();) {
			WeakReference<FileListener> reference = i.next();
			FileListener listener = reference.get();
			if (listener == fileListener) {
				i.remove();
				break;
			}
		}
	}

	private class FileMonitorNotifier extends TimerTask {
		public void run() {
			// Loop over the registered files and see which have changed.
			// Use a copy of the list in case listener wants to alter the
			// list within its fileChanged method.
			Collection<File> files = new ArrayList<File>(files_.keySet());

			for (Iterator<File> i = files.iterator(); i.hasNext();) {
				File file = i.next();
				long lastModifiedTime = ((Long) files_.get(file)).longValue();
				long newModifiedTime = file.exists() ? file.lastModified() : -1;

				// Check if file has changed
				if (newModifiedTime != lastModifiedTime) {

					// Register new modified time
					files_.put(file, new Long(newModifiedTime));

					// Notify listeners
					for (Iterator<WeakReference<FileListener>> j = listeners_
							.iterator(); j.hasNext();) {
						WeakReference<FileListener> reference = j.next();
						FileListener listener = (FileListener) reference.get();

						// Remove from list if the back-end object has been GC'd
						if (listener == null)
							j.remove();
						else
							listener.fileChanged(file);
					}
				}
			}
		}
	}
}
