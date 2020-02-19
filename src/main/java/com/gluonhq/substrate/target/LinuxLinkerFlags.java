package com.gluonhq.substrate.target;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.gluonhq.substrate.target.LinuxFlavor.LINUX_FLAVOR;
import com.gluonhq.substrate.util.Logger;
import com.gluonhq.substrate.util.ProcessRunner;

import static com.gluonhq.substrate.target.LinuxLinkerFlags.PkgInfo.*;

public class LinuxLinkerFlags {
	static final LINUX_FLAVOR flavor = new LinuxFlavor().getFlavor();

	/**
	 * Information about a compilation package.
	 * 
	 * @param pkgName name as used by pkg-config
	 * @param installName name as used by OS package manager
	 */
	static class PkgInfo {
		final String pkgName;
		final String installName;
		final String hardwired;
	
		private PkgInfo(String pkgName, String installName, String hardwired) {
			this.pkgName = pkgName;
			this.installName = installName;
			this.hardwired = hardwired;
		}
		
		public static PkgInfo debian(String pkgName, String installName) {
			return new PkgInfo(pkgName, installName, null);
		}

		public static PkgInfo fedora(String pkgName, String installName) {
			return new PkgInfo(pkgName, installName, null);
		}

		public static PkgInfo activeOf(PkgInfo debian, PkgInfo fedora) {
			return flavor.isDebNaming() ? debian : fedora;
		}
		
		public static PkgInfo hardwired(String flag) {
			return new PkgInfo("", "", flag);
		}
	}

	private static final List<PkgInfo> LINK_DEPENDENCIES = List.of(
			hardwired("-Wl,--no-whole-archive"),

			activeOf(debian("gl", "libgl-dev"), 				fedora("gl", "mesa-libGL-devel")),
			activeOf(debian("x11", "libx11-dev"), 				fedora("x11", "libX11-devel")),
			activeOf(debian("gtk+-x11-3.0", "libgtk-3-dev"),	fedora("gtk+-3.0", "gtk3-devel")),
			activeOf(debian("gtk+-x11-3.0", "libgtk-3-dev"),	fedora("gtk+-3.0", "gtk3-devel")),
			activeOf(debian("freetype2", "libfreetype6-dev"),	fedora("freetype2", "freetype-devel")),
			activeOf(debian("pangoft2", "libpango1.0-dev"),		fedora("pangoft2", "pango-devel")),
			
			hardwired("-lgstreamer-lite"),
			
			activeOf(debian("gthread-2.0", "libglib2.0-dev"),	fedora("gthread-2.0", "glib2-devel")),
			
			hardwired("-lstdc++"),
			activeOf(debian("zlib", "zlib1g-dev"), 				fedora("zlib", "zlib-devel")),
			
			activeOf(debian("xtst", "libxtst-dev"), 			fedora("xtst", "libXtst-devel")),

			// On fedora these require https://rpmfusion.org/
			activeOf(debian("libavcodec", "libavcodec-dev"), 	fedora("libavcodec", "ffmpeg-devel")),
			activeOf(debian("libavformat", "libavformat-dev"), 	fedora("libavformat", "ffmpeg-devel")),
			activeOf(debian("libavutil", "libavutil-dev"),	 	fedora("libavutil", "ffmpeg-devel")),

			activeOf(debian("alsa", "libasound2-dev"),	 		fedora("alsa", "alsa-lib-devel")),

			hardwired("-lm"),
			
			activeOf(debian("gmodule-no-export-2.0", "libglib2.0-dev"),	fedora("gmodule-no-export-2.0", "glib2-devel"))
		);
	
	
	public List<String> getLinkerFlags() {
		List<String> pkgFlags = LINK_DEPENDENCIES.stream()
			.flatMap(pkg -> lookupPackageFlags(pkg).stream())
			.collect(Collectors.toList());
		
		Logger.logInfo("All flags: " + pkgFlags);
		
		return pkgFlags;
	}

	private List<String> lookupPackageFlags(PkgInfo pkgInfo) {
		if (pkgInfo.hardwired != null) {
			return List.of(pkgInfo.hardwired);
		}
		
		String pkgName = pkgInfo.pkgName;
		ProcessRunner process = new ProcessRunner("/usr/bin/pkg-config", "--libs", pkgName);
        try {
			if (process.runProcess("Get config for " + pkgName) != 0) {
				throw new IllegalStateException("Package " + pkgName + " not present for linking, please install " + pkgInfo.installName);
			}
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Failed to lookup linker flags", e);
		}
        
        List<String> flags = List.of(process.getResponse().trim().split(" "));
        Logger.logInfo("Pkg " + pkgName + " provided flags: " + flags);
		return flags;
	}
	
	public static void main(String[] args) {
		System.out.println("output: " + new LinuxLinkerFlags().getLinkerFlags());
	}
}
