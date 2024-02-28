

for ((i = 1; i <= 16; i += 2)); do
	/home/newton/IdeaProjects/kmp-native-wizard/memory.exe &
	stress-ng --mmaphuge $i --mmaphuge-mmaps 65536 -t 10
	kill -SIGINT %%
done
