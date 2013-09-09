/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package sun.tools.attach;

import java.io.IOException;
import java.util.List;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

public class OperatingSystemAwareAttachProvider extends AttachProvider {

	private static String OS = System.getProperty("os.name").toLowerCase();
	
	private AttachProvider attachProvider;
	
	public OperatingSystemAwareAttachProvider() {
		try {
			if (isWindows()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.WindowsAttachProvider").newInstance();
			}
			else if (isLinux()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.LinuxAttachProvider").newInstance();
			}
			else if (isMac()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.MacosxAttachProvider").newInstance();
			}
			else if (isSolaris()) {
				attachProvider = (AttachProvider) Class.forName("sun.tools.attach.SolarisAttachProvider").newInstance();
			}
			else {
				throw new RuntimeException("There is no supported AttachProvider for current operating system: " + OS);
			}
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private boolean isWindows() {
		return OS.indexOf("win") >= 0;
	}
	
	private boolean isLinux() {
		return OS.indexOf("nux") >= 0;
	}
		
	private boolean isMac() {
		return OS.indexOf("mac") >= 0;
	}

	private boolean isSolaris() {
		return OS.indexOf("sunos") >= 0;
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
