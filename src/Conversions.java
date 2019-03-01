
public class Conversions 
{
	public static int hexToDecimal(String hex) 
	{
		return Integer.parseInt(hex, 16);
	}
	
	public static String decimalToHex(int decimal) 
	{
		return Integer.toHexString(decimal);
	}
	
	public static String hexStringToBinaryString(String hex) 
	{
		long i = Long.parseLong(hex, 16);
		String binaryString = Long.toBinaryString(i);
		
		while (binaryString.length() != 32) 
		{
			binaryString = "0" + binaryString;
		}
		
		return binaryString;
	}
	
	public static int binaryStringToLiteralInteger(String binary) 
	{
		int retVal = Integer.parseInt(binary, 2);
		return retVal;
	}
		
}
