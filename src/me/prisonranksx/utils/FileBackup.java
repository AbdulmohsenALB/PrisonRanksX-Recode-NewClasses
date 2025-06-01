package me.prisonranksx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class FileBackup {

	private File file;

	public FileBackup(File file) {
		this.file = file;
	}

	/**
	 * Creates a backup of the file with the given extension in the same directory with the same file name but
	 * the current time, a random number of 6 digits, and ".backup" appended. For example, if the file is called "file.txt" and the
	 * current time is "2020-01-01 00:00:00" and the random number is 123456, then the backup file will be called "file 2020_01_01T00_00_00 123456.txt.backup"
	 *
	 * @param file The file to create a backup of
	 * @return A CompletableFuture that completes when the backup is created. If there is no file to create a backup of, then the CompletableFuture completes immediately
	 */
	public static CompletableFuture<Void> fromFile(File file) {
		return new FileBackup(file).createBackup(".backup");
	}

	/**
	 * Creates a backup of the directory with the same name but the current time, a random number of 6 digits, and "_backup" appended.
	 *
	 * @param file The directory to create a backup of
	 * @return A CompletableFuture that completes when the backup is created. If there is no directory to create a backup of, then the CompletableFuture completes immediately
	 */
	public static CompletableFuture<Void> fromDirectory(File file) {
		return new FileBackup(file).createDirectoryBackup();
	}

	/**
	 * Creates a backup of the file with the given extension in the same directory with the same file name but
	 * the current time and a random number of 6 digits appended. For example, if the file is called "file.txt" and the
	 * current time is "2020-01-01 00:00:00" and the random number is 123456, then the backup file will be called "file 2020_01_01T00_00_00 123456.txt.(extension)"
	 *
	 * @param extension The extension of the backup file
	 * @return A CompletableFuture that completes when the backup is created
	 */
	public CompletableFuture<Void> createBackup(String extension) {
		String timeFormatted = LocalDateTime.now().toString()
				.replace(":", "_")
				.replace("-", "_")
				.replace(".", "_");
		return createBackupAs(file.getName() + " " + timeFormatted + " " + ThreadLocalRandom.current().nextInt(0, 999999) + extension);
	}

	/**
	 * Creates a backup of the file with the given name in the same directory
	 *
	 * @param newFileName file name with the extension
	 * @return A CompletableFuture that completes when the backup is created
	 */
	public CompletableFuture<Void> createBackupAs(String newFileName) {
		if (!file.exists()) return CompletableFuture.completedFuture(null);
		return CompletableFuture.runAsync(() -> {
			try {
				Files.copy(file.toPath(), file.toPath().getParent().resolve(newFileName));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Creates a backup of the file with the given name in the given directory
	 *
	 * @param newFileName file name with the extension
	 * @param directory   directory where backup file will be created
	 * @return A CompletableFuture that completes when the backup is created
	 */
	public CompletableFuture<Void> createBackupAs(String newFileName, String directory) {
		if (!file.exists()) return CompletableFuture.completedFuture(null);
		return CompletableFuture.runAsync(() -> {
			try {
				Files.copy(file.toPath(), new File(directory, newFileName).toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Creates a *recursive* copy of an existing directory, producing a new directory
	 * with a timestamp/random suffix, and mirroring all subfolders & files.
	 */
	public CompletableFuture<Void> createDirectoryBackup() {
		if (!file.exists()) {
			return CompletableFuture.completedFuture(null);
		}
		if (!file.isDirectory()) {
			// If it's not a directory, we can either bail or fall back to a single-file copy.
			// For safety, let’s call createBackup(".backup") instead:
			return createBackup(".backup");
		}

		return CompletableFuture.runAsync(() -> {
			try {
				String timeFormatted = LocalDateTime.now().toString()
						.replace(":", "_")
						.replace("-", "_")
						.replace(".", "_");
				String newDirName = file.getName()
						+ " "
						+ timeFormatted
						+ " "
						+ ThreadLocalRandom.current().nextInt(0, 999999)
						+ "_backup";

				Path sourcePath = file.toPath();
				Path targetParent = sourcePath.getParent();
				Path targetPath = targetParent.resolve(newDirName);

				copyDirectoryRecursively(sourcePath, targetPath);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Walks the source directory tree and copies each file/subdirectory into the target location.
	 */
	private void copyDirectoryRecursively(Path source, Path target) throws IOException {
		// Ensure the root of the backup directory exists
		Files.createDirectories(target);

		// Walk the file tree
		Files.walkFileTree(source, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException {
				Path relative = source.relativize(dir);
				Path newDir = target.resolve(relative);
				Files.createDirectories(newDir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs)
					throws IOException {
				Path relative = source.relativize(filePath);
				Path newFile = target.resolve(relative);
				// Copy file attributes + data
				Files.copy(
						filePath,
						newFile,
						StandardCopyOption.COPY_ATTRIBUTES,
						StandardCopyOption.REPLACE_EXISTING
				);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public CompletableFuture<Void> createDirectoryBackupAs(String newDirectoryName) {
		if (!file.exists()) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.runAsync(() -> {
			try {
				Path sourcePath = file.toPath();
				Path targetParent = sourcePath.getParent();
				Path targetPath = targetParent.resolve(newDirectoryName);
				copyDirectoryRecursively(sourcePath, targetPath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public File getFile() {
		return file;
	}
}
