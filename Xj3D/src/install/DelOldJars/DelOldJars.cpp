// DelOldJars.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>

void confirm(char * fullpath, int confirm) {
	int ch;
	if (confirm == 'c' || confirm == 'C') {
		printf("\nRemove %s [y/N]: ", fullpath);
		ch = getchar();
		if (ch != 10)
    		getchar();  // Remove return

		if (ch == 'y' || ch == 'Y') {
			printf("Removing %s\n", fullpath);
			remove(fullpath);
		}
	}
	else {
		printf("Removing %s\n", fullpath);
		remove(fullpath);
	}
}

void removeXj3dJars(char * path) {
	char fullpath[120];
	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-common.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-core.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-eai.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-ecmascript.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-j3d.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-jaxp.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-jsai.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-net.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-norender.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-parser.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-render.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-runtime.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-sai.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-sav.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-script-base.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-xml-util.jar");
	printf("Removing %s\n", fullpath);
	remove(fullpath);
}

void removeJars(char * path, int conf) {
	char fullpath[120];
	int ch;

	strcpy(fullpath, path);
	strcat(fullpath, "imageloader.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "xj3d-*.jar");

	if (conf == 'c' || conf == 'C') {
		printf("\nRemove %s [y/N]: ", fullpath);
		ch = getchar();
		if (ch != 10)
    		getchar();  // Remove return

		if (ch == 'y' || ch == 'Y') {
			printf("Removing %s\n", fullpath);
			removeXj3dJars(path);
		}
	}
	else {
		printf("Removing %s\n", fullpath);
		removeXj3dJars(path);
	}

	strcpy(fullpath, path);
	strcat(fullpath, "SaiX3d.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "dtdparser113a.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "gnu-regexp-1.0.8.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "j3d-org-images.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "j3d-org-java3d-all_0.9.0.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "uri.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "vlc_uri.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "js.jar");
	confirm(fullpath, conf);

	strcpy(fullpath, path);
	strcat(fullpath, "httpclient.jar");
	confirm(fullpath, conf);

}

int main(int argc, char* argv[])
{
	int ch;
	char path[120];

	if (argc < 2) {
		printf("Invalid usage.  Provide JAVA directory\n");
		strcpy(path,"c:\\test\\");
	}
	else {
		strcpy(path, argv[1]);
		strcat(path,"lib\\ext\\");
		printf("Removing files from: %s\n", path);
	}

	printf("Installer must delete old Xj3D jars before continuing upgrade.\n  Do you wish to D) Delete all I) Ignore or C) Confirm each deletion: [d,i,c]: ");
	ch = getchar();

	if (ch == 'i' || ch == 'I') {
		printf("No files deleted\n");
		return 0;
	}
	if (ch != 10)
		getchar();

	removeJars(path, ch);

	return 0;
}

