/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.substrate.target;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Determines flavor of linux from os-release 
 */
public class LinuxFlavor {
    private static final Pattern OS_RELEASE_PROPERTY_PATTERN = Pattern.compile("([A-Z_-]+)=\"?(.+)\"?");

    public static void main(String[] args) {
		System.out.println("Is on " + new LinuxFlavor().getFlavor());
	}
    
    public enum PACKAGING_SYSTEM {
    	DEB,
    	RPM
    }
    
    public enum LINUX_FLAVOR {
    	DEBIAN(PACKAGING_SYSTEM.DEB),
    	FEDORA(PACKAGING_SYSTEM.RPM);
    	
    	private final PACKAGING_SYSTEM packagingSystem;

		private LINUX_FLAVOR(PACKAGING_SYSTEM packagingSystem) {
			this.packagingSystem = packagingSystem;
    	}

		public boolean isUsingDeb() {
			return packagingSystem == PACKAGING_SYSTEM.DEB;
		}

		public boolean isUsingRpm() {
			return packagingSystem == PACKAGING_SYSTEM.RPM;
		}
    }

    public LINUX_FLAVOR getFlavor() {
    	List<String> osReleaseLines = readOsRelease();
    	if (osReleaseLines.stream().anyMatch(l -> isFedora(l))) {
    		return LINUX_FLAVOR.FEDORA;
		}
    	return LINUX_FLAVOR.DEBIAN;
    }
    
    private List<String> readOsRelease() {
    	return List.of("/etc/os-release", "/usr/lib/os-release").stream()
    		.map(Paths::get)
    		.filter(Files::exists)
    		.map(this::readAllLines)
    		.findFirst()
    		.orElse(List.of());
    }
    
    private List<String> readAllLines(Path file) {
    	try {
    		return Files.readAllLines(file);
    	} catch (IOException e) {
    		// tried, but handle silently
    		return List.of();
    	}
    }
    
    private boolean isFedora(String l) {
    	var matcher = OS_RELEASE_PROPERTY_PATTERN.matcher(l);
    	if (!matcher.matches()) {
    		return false;
    	}
		String key = matcher.group(1);
		String value = matcher.group(2);
		
		if ("ID".equals(key) || "ID_LIKE".equals(key)) {
			return value.contains("fedora") || value.contains("rhel");
		}

		return false;
    }
}
