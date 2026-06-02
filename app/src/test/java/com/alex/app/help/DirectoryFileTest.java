package com.alex.app.help;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class DirectoryFileTest {
	@Mock
	MyFile thisFile;
	
	private DirectoryFile tester;
	
	@BeforeEach
	public void setup() {
		Path p = Path.of("E:\\FF");
		Mockito.doReturn("E:\\FF").when(thisFile).toString();
		Mockito.doReturn(p).when(thisFile).toPath();
		tester = new DirectoryFile(thisFile, TypeFile.dir);
		tester = Mockito.spy(tester);
	}
	
	@Test
	public void testDirectoryFile() {
		
		System.out.println(tester.getRoot());
		
		Mockito.verify(tester, Mockito.atLeast(1)).getRoot();
//		assertEquals("результат ", tester.getName());
	}
}
