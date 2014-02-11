% list = ls ( '../dists/distance-top200*.txt' );
list = char('../dists/distance-top200573.txt', ...
    '../dists/distance-top200510.txt', ...
    '../dists/distance-top2003754.txt', ...
    '../dists/distance-top2002241.txt');
         

 
range = 0:1:80;
for i = 1:size(list,1)
    a = importdata(['../dists/', list(i,:)]);

    figure; 
    N = hist( a, range );
    plot( range, N ./ numel( a ) );
    title(list(i,:));
end


% set(gca, 'yscale', 'log');
% set(gca, 'xscale', 'log');