//Standard libraries used
use mbed::tcp_ip;
use std::env;
fn main() {
    //Extract protocol
    let args: Vec<String> = env::args().collect();
    let proto_str = (&args[1]).to_uppercase(); // TCP/UDP
    let proto = match &proto_str[..] {
        "TCP" => tcp_ip::TLProtocol::TCP,
        "UDP" => tcp_ip::TLProtocol::UDP,
        _ => panic!("Undefined Protocol"),
    };

    println!("\nStarting {} CLIENT...\n", proto_str);

    //Initialize context
    let mut context = tcp_ip::MbedtlsNetContext::new(proto);

    //Establish connection with server
    println!("Trying to connect to server at 127.0.0.1:4442...");
    let ret = tcp_ip::mbedtls_net_connect(&mut context, "127.0.0.1", "4442", &proto);
    if ret != tcp_ip::MBEDTLS_NET_OPER_SUCCESS {
        println!("Failed to connect to server.\n");
    } else {
        loop {
            println!("Enter message:");
            let mut msg = String::new();
            std::io::stdin().read_line(&mut msg).unwrap();

            //Send message to server
            println!("Sending message to server...");
            let ret = tcp_ip::mbedtls_net_send(&mut context, (msg[..]).as_bytes());
            if ret != tcp_ip::MBEDTLS_NET_OPER_SUCCESS {
                println!("Failed to send message.\n");
                break;
            }

            //Recieve message from server
            let mut buf: [u8; 512] = [0; 512];
            let ret = tcp_ip::mbedtls_net_recv(&mut context, &mut buf, 512);
            if ret != tcp_ip::MBEDTLS_NET_OPER_SUCCESS {
                println!("Failed to recieve message.\n");
                break;
            }
            println!("Response received: {}", String::from_utf8_lossy(&buf));
        }
    }

    println!("Client exiting...\n");
}
