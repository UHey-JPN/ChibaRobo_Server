package console;

import java.io.IOException;

/**
 * コマンドを受け取って、実行するインターフェース。
 *
 */
public interface CommandProcesser {
	/**
	 * このコマンドを実行する名前を返す。
	 * 
	 * @return - Stringでコマンド名。
	 */
	public String get_cmd_name();
	
	/**
	 * コマンドを受け取って、コマンドを実行。
	 * 
	 * @param cmd - String[]でコマンドライン引数的なものを与える。
	 * @return - booleanでログイン状態継続かを返す。
	 * @throws Exception - 万が一に備えて、例外を拾えるように。
	 * @throws IOException - 万が一に備えて、例外を拾えるように。 
	 */
	public boolean command_process(String[] cmd) throws Exception;
}
