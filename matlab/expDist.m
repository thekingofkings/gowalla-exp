
dist1 = 0:0.02:200;
% dist2 = 100:50:5000;

figure();
hold on;
set(0, 'defaultlinelinewidth', 3)
plot(dist1, exp( -0.01 * dist1 ), 'm--');
plot(dist1, exp( -0.05 * dist1 ), 'r-');
plot(dist1, exp( -0.1 * dist1 ), 'k--');
plot(dist1, exp( - dist1 ), 'b-');
plot(dist1, exp( - 2 * dist1 ), 'c-');
plot(dist1, exp( - 3 * dist1 ), 'g-.');
plot(dist1, exp( -5 * dist1 ), 'c--');
plot(dist1, exp( - 10 * dist1 ), 'r-.');

legend({'0.01', '0.05', '0.1', '1', '2', '3', '5', '10'}, 'fontsize', 16);
% set(gca, 'yscale', 'log');



