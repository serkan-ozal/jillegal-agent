/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
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
			else if (OsUtil.isLinux()) {
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
