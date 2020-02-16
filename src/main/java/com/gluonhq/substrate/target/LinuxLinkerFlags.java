package com.gluonhq.substrate.target;

import java.io.IOException;
import java.util.List;

import com.gluonhq.substrate.target.LinuxFlavor.LINUX_FLAVOR;
import com.gluonhq.substrate.util.ProcessRunner;

import static com.gluonhq.substrate.target.LinuxLinkerFlags.PkgInfo.*;

public class LinuxLinkerFlags {

	/**
	 * Package definitions for different linux variants.
	 */
	static class Pkg {
		static final LINUX_FLAVOR flavor = new LinuxFlavor().getFlavor();
		
		private final PkgInfo activeInfo;

		public Pkg(PkgInfo debian, PkgInfo fedora) {
			activeInfo = flavor.isDebNaming() ? debian : fedora;
		}
		
		public PkgInfo getActiveInfo() {
			return activeInfo;
		}
	}

	/**
	 * Information about a compilation package.
	 * 
	 * @param pkgName name as used by pkg-config
	 * @param installName name as used by OS package manager
	 */
	static class PkgInfo {
		final String pkgName;
		final String installName;
	
		public PkgInfo(String pkgName, String installName) {
			this.pkgName = pkgName;
			this.installName = installName;
		}
		
		public static PkgInfo debian(String pkgName, String installName) {
			return new PkgInfo(pkgName, installName);
		}

		public static PkgInfo fedora(String pkgName, String installName) {
			return new PkgInfo(pkgName, installName);
		}
	}
	
	List<Pkg> linkDependencies = List.of(
			new Pkg(debian("xtst", "libxtst-dev"), fedora("xtst", "libXtst-devel"))
			);
			
	
	
	public List<String> getLinkerFlags() throws IOException, InterruptedException {
		String lib = "xtst";
		
		PkgInfo pkg = linkDependencies.get(0).getActiveInfo();
		
		String flags = lookupPackageFlags(pkg);
		
		System.out.println(" '" + flags + "'");
		
        return List.of();
	}

	private String lookupPackageFlags(PkgInfo pkgInfo) throws IOException, InterruptedException {
		String pkgName = pkgInfo.pkgName;
		ProcessRunner process = new ProcessRunner("/usr/bin/pkg-config", "--libs", pkgName);
        if (process.runProcess("Get config for " + pkgName) != 0) {
        	throw new IllegalStateException("Package " + pkgName + " not present for linking, please install " + pkgInfo.installName);
        }
        
        return process.getResponse().trim();
	}
	
	public static void main(String[] args) {
		try {
			System.out.println("output: " + new LinuxLinkerFlags().getLinkerFlags());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
