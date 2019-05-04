package io.hankers.mdi.philips_center;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
		new DataListener().start();
    }
}
