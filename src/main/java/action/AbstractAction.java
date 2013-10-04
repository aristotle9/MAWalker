package action;

import walker.ErrorData;

public abstract class AbstractAction {
	protected ErrorData errorData;
	protected walker.Process process;

	public walker.Process getProcess() {
		return process;
	}

	public void setProcess(walker.Process process) {
		this.process = process;
	}

	public ErrorData getErrorData() {
		return errorData;
	}

	public void setErrorData(ErrorData errorData) {
		this.errorData = errorData;
	}
	
	public abstract boolean run() throws Exception;
}
