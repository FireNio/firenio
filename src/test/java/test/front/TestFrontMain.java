package test.front;

import java.io.IOException;

import com.gifisan.nio.front.FrontFacade;

public class TestFrontMain {

	public static void main(String[] args) throws IOException {

		FrontFacade facade = new FrontFacade();

		facade.start();
	}
}
