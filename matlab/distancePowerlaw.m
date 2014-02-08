list = ls ( '../dists/*.txt' );

figure;
hold on;    
range = 0:20:6200;
for i = 1:10
    a = importdata(['../dists/', list(i,:)]);


    N = hist( a, range );
    plot( range, N ./ numel( a ) );
    hold on;
end


set(gca, 'yscale', 'log');
set(gca, 'xscale', 'log');