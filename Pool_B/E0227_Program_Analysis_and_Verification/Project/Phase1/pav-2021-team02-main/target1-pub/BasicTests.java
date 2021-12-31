class BasicTest1
{
    static int myIncrement(int x) {
        return x + 1;
    }

    static int add_x(int flag)
    {
        int x = 0;
        int sum = 0;
        if (flag == 0) {
            x = x + 10;
        }
        sum = sum + x;
        sum = sum * 3;
        return sum;
    }

    static int mySum() {
        int sum = 0;
        for(int i = 0; i <= 10; i++) {
            sum += i;
        }

        return sum;
    }

    static int myChoose(int flag)
    {
        int x = 0;
        if (flag == 0) {
            x = 10;
        } else {
            x = 20;
        }
        return x;
    }

    public static void main(String args[])
    {
        int y1;


        y1 = mySum();
    }
}

