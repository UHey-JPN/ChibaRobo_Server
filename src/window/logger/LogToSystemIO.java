package window.logger;

public class LogToSystemIO implements LogMessageAdapter {

	@Override
	public void log_println(String x) {
		System.out.println(x);
	}

	@Override
	public void log_print(Exception e) {
		e.printStackTrace();
	}

}
