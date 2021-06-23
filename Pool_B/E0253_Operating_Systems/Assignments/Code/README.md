
OS Assignment
-------------------------------------------------------------


1) Clone into kernel code repository:
-------------------------------------------------------------

	wget https://cdn.kernel.org/pub/linux/kernel/v5.x/linux-5.11.5.tar.xz


2) Apply kernel patch:
-------------------------------------------------------------

	cd <linux_source_folder>
	git init
	git add .
	git commit -am "Initial Commit"
	git am <patch_file_name>

Patches available in /Submissions/17920_e0253_# directory.


3) Compile kernel:
-------------------------------------------------------------

	make ARCH=x86_64 x86_64_defconfig
	make menuconfig

	For using GDB:
		Goto "Kernel Hacking" -> 
		Goto "Compile time checks and compiler options" -> 
		Enable "Provide GDB scripts for kernel debugging"

	For Assignment 2 & 3:
		Goto "Memory Management options" ->
		Enable "Idle page tracking"

	For Assignment 3:
		Goto "Memory Management options" ->
		Enable "Transparent Huge Page support"

	make -j4


4) Creating filesystem image using buildroot:
-------------------------------------------------------------

Follow instructions from below link:
	https://medium.com/@daeseok.youn/prepare-the-environment-for-developing-linux-kernel-with-qemu-c55e37ba8ade


5) Moving your files into filesystem: Overlays
-------------------------------------------------------------
	
	cd <buildroot_folder>

	mkdir <my_dir>
	Copy files into <my_dir>

	make menuconfig
		Goto "System Configuration"
		Goto "Root filesystem overlay directories"
		Provide name of folder <my_dir>

	Compile buildroot: make -j4

	NOTE: All C object files must be compiled statically using --static flag

To run "Ballooning application", move "main.c" (available in /Submissions/17920_e0253_# directory) to <my_dir>.


6) Running kernel:
-------------------------------------------------------------

	qemu-system-x86_64 \
	-kernel  <path_to_top_of_kernel_source>/linux-5.11.5/arch/x86/boot/bzImage \
	-hda     <path_to_top_of_buildroot_dir>/buildroot/output/images/rootfs.ext2 \
	-append  "root=/dev/sda rw console=ttyS0,115200 acpi=off nokaslr" \
	-boot    c \
	-m       1.75G \
	-serial  stdio \
	-display none 


7) Making swap:
-------------------------------------------------------------

	cd <buildroot_folder>

	make menuconfig
		Goto "Filesystem images"
		Specify size of filesystem in "exact size" (for e.g. 4G)

	Compile buildroot: make -j4

	Run Kernel
	Make swap using:
		cd / && \
		dd if=/dev/zero of=/swapfile bs=1K count=1M && \
		chmod 600 /swapfile && \
		mkswap /swapfile && \
		swapon /swapfile 


8) Debugging Kernel
-------------------------------------------------------------

	Open 1st terminal:

	Run kernel using:
		qemu-system-x86_64 \
		-s -S \
		-kernel  <path_to_top_of_kernel_source>/linux-5.11.5/arch/x86/boot/bzImage \
		-hda     <path_to_top_of_buildroot_dir>/buildroot/output/images/rootfs.ext2 \
		-append  "root=/dev/sda rw console=ttyS0,115200 acpi=off nokaslr" \
		-boot    c \
		-m       2G \
		-serial  stdio \
		-display none 

	Open 2nt terminal:
	
	cd <linux_source_folder>
	gdb ./vmlinux
	target remote localhost:1234

	break <function_name> (for e.g. break shrink_page_list)
	i b  (Display all breakpoints)
	disa (Disable all breakpoints)
	c (continue execution to next breakpoint)
	n (execute single instruction)


9) Create Patch
-------------------------------------------------------------
	
	cd <linux_source_folder>
	git init
	git add .
	git commit -am "Initial Commit"

	<stage files> (Add all new files and modifications)
	git add .
	git commit -s -m "New Commit"
	
	Get commit_id of last commit (for e.g. )
	git format-patch <commit_id> -<number of previous commits to include in patch>
	For e.g. git format-patch c5a9e40bfb9cb7e7cb23af21c155e4057a8d8e05 -1


10) Encrypt directory using public key
-------------------------------------------------------------

	zip -r <my_dir>.zip <my_dir>/
	openssl smime -encrypt -binary -aes-256-cbc -in <my_dir>.zip -out <my_dir>.enc -outform DER e0253.cert
