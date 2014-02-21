% plot exponentail decay with different distance parameters c.
dist1 = 0:0.02:500;
% dist2 = 100:50:5000;

figure();
hold on;
box on;
grid on;

plot(dist1, exp( -0.01 * dist1 ), '--', 'color', [1, 0.7, 0.75]);
plot(dist1, exp( -0.1 * dist1 ), '--', 'color', [255, 69, 0]/255);
plot(dist1, exp( -0.2 * dist1 ), '--', 'color', [160, 0, 0] / 255);
plot(dist1, exp( - dist1 ), '-', 'color', [0.5, 0.8, 1]);
plot(dist1, exp( - 2 * dist1 ), '-', 'color', [0.3, 0.4, 1] );
plot(dist1, exp( - 3 * dist1 ), '-', 'color', [0, 0, 0.5] );
plot(dist1, exp( -5 * dist1 ), '-.', 'color', [144, 238, 144]/ 255);
plot(dist1, exp( - 10 * dist1 ), '-.', 'color', [0, 128, 0]/255 );


axis([0.01, 1000, -0.01, 1.01]);
hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 4);
legend({'0.01', '0.1', '0.2', '1', '2', '3', '5', '10'}, 'fontsize', 16, ...
    'location', 'northeast');
set(gca, 'xscale', 'log', 'linewidth', 2, 'fontsize', 18, ...
    'xtick', [0.01, 0.1, 1, 10, 100, 1000]);
xlabel('Distance (km) or Time (day)', 'fontsize', 20, 'interpreter', 'latex');
ylabel(' ', 'fontsize', 20);
print('expDecay.eps', '-dpsc');
system('epstopdf expDecay.eps');


