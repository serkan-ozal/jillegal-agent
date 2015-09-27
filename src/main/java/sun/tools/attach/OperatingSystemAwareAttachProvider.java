/*
 * Copyright (c) 1986-2015, Serkan OZAL, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sun.tools.attach;

import java.io.IOException;
import java.util.List;

import tr.com.serkanozal.jillegal.agent.util.LogUtil;
import tr.com.serkanozal.jillegal.agent.util.OsUtil;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

public class OperatingSystemAwareAttachProvider extends AttachProvider {

	private AttachProvider attachProvider;
	
	public OperatingSystemAwareAttachProvider() {
		try {
			if (OsUtil.isWindows()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.WindowsAttachProvider").newInstance();
			}
			else if (OsUtil.isUnix()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.LinuxAttachProvider").newInstance();
			}
			else if (OsUtil.isMac()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.BsdAttachProvider").newInstance();
			}
			else if (OsUtil.isSolaris()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.SolarisAttachProvider").newInstance();
			}
			else {
				throw new RuntimeException("There is no supported AttachProvider for current operating system: " + OsUtil.OS);
			}
			LogUtil.debug("Using attach provider: " + attachProvider);
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	@Override
	public VirtualMachine attachVirtualMachine(String id) throws AttachNotSupportedException, IOException {
		return attachProvider.attachVirtualMachine(id);
	}

	@Override
	public List<VirtualMachineDescriptor> listVirtualMachines() {
		return attachProvider.listVirtualMachines();
	}

	@Override
	public String name() {
		return attachProvider.name();
	}

	@Override
	public String type() {
		return attachProvider.type();
	}

}
