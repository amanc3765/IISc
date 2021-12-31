class MyTargetClass1
{
    static void test1Conditional(int x){
        if(x <= 0){
            if(x > -10){
                if(x == -5){
                }else{
                }
            }else{
                if(x <= 15){
                }else{
                }
            }
        }else{
            if(x < 10){
            }else{
            }
        }

        return;
    }

    static int test2Widening(){
        int x = 0;
        int y = 0;
        
        while(x<=10){
            x++;
            y--;
        }

        return x+y;
    }

    static int test3NestedLoops(){
        int x = 0;
        int y = 0;
        int sum = 0;
        for(;x<2; ++x){
            for(;y<2; ++y)
                sum = sum + y;
        }

        return sum;
    }

    static int test4UnaryAssignment(int x){
        int y=x+1;
        if(x<5){
            y = -x;
        }
        else {
            y = 1-x;
        }
        
        return y;
    }

    static int test5BinaryExpressionsAdd(int x){
        int y;
        if(x<10){
            y = x+100000;
        }
        else{
            y = x+2;
        }
        return y;
    }

    static int test6BinaryExpressionsSubtract(int x){
        int y;
        if(x<10){
            y = x-100000;
        }
        else{
            y = x-2;
        }
        return y;
    }

    static int test7BinaryExpressionsMultiply(int x){
        int y;
        if(x<10){
            y = x*100000;
        }
        else{
            y = x*2;
        }
        return y;
    }
}

class MyTargetClass2
{
}

